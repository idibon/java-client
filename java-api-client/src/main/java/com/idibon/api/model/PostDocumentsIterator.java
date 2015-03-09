/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import javax.json.*;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Background batch document upload process. Runs independently until
 * completion or an error occurs.
 */
class PostDocumentsIterator implements Iterator<Document> {

    public boolean hasNext() {
        return _contentToPost.hasNext() ||
            !_submitQueue.isEmpty() ||
            !_resultQueue.isEmpty();
    }

    public Document next() {
        Document nextDoc = _resultQueue.pollFirst();
        if (nextDoc == null) {
            waitForNextBatch();
            nextDoc = _resultQueue.pollFirst();
            if (nextDoc == null)
                throw new NoSuchElementException("No more documents");
        }
        try {
            advance();
        } catch (IOException ex) {
            throw new IterationException("Error posting data to API", ex);
        }
        return nextDoc;
    }

    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Consume any completed batch uploads, then dispatch more if free
     * slots are available.
     */
    void advance() throws IOException {
        /* search for any complete results, grab the results, and dispatch
         * additional requests */
        for (Future<JsonValue> head = _submitQueue.peek();
               head != null && head.isDone(); head = _submitQueue.peek()) {
            waitForNextBatch(); // no wait actually needed
        }

        long estimatedSize = 0;
        JsonArrayBuilder batch = null;

        /* For performance, upload documents in batches, and upload multiple
         * batches in parallel. Empirically, the best performance seems to be
         * when the upload batch size 25KiB - 100KiB. */
        while (_submitQueue.size() < SUBMIT_LIMIT && _contentToPost.hasNext()) {
            // create a new array for this batch of documents, if needed
            if (batch == null) batch = JSON_BF.createArrayBuilder();

            JsonObject object = Util.toJson(_contentToPost.next());
            batch.add(object);
            estimatedSize += Util.estimateSizeOfJson(object);

            if (estimatedSize >= BATCH_UPLOAD_TARGET) {
                submitBatch(batch);
                batch = JSON_BF.createArrayBuilder();
                estimatedSize = 0;
            }
        }

        if (estimatedSize > 0)
            submitBatch(batch);
    }

    /**
     * Waits for the next batch upload to complete and parse the results.
     */
    private void waitForNextBatch() {
        // throw an exception if there isn't a batch
        Future<JsonValue> nextBatch = _submitQueue.removeFirst();
        JsonValue result;
        try {
            result = nextBatch.get();
        } catch (ExecutionException ex) {
            throw new IterationException("API operation failed", ex.getCause());
        } catch (InterruptedException ex) {
            throw new IterationException("API op interrupted", ex.getCause());
        }

        if (!(result instanceof JsonObject))
            throw new IterationException("Unexpected API response");

        JsonArray documents = ((JsonObject)result).getJsonArray("documents");

        if (documents == null)
            throw new IterationException("API response contained no data");

        for (JsonObject doc : documents.getValuesAs(JsonObject.class))
            _resultQueue.add(_collection.document(doc.getString("name")));
    }

    /**
     * POST a batch of documents to the API.
     */
    private void submitBatch(JsonArrayBuilder batch) throws IOException {
        JsonObject body = JSON_BF.createObjectBuilder()
            .add("documents", batch)
            .build();

        String ep = _collection.getEndpoint() + "/*";
        _submitQueue.add(_collection.getInterface().httpPost(ep, body));
    }

    PostDocumentsIterator(Collection collection,
          Iterator<? extends DocumentContent> contentToPost)
          throws IOException {
        _contentToPost = contentToPost;
        _collection = collection;
        advance();
    }

    // Collection being posted to
    private final Collection _collection;

    // Content to post
    private final Iterator<? extends DocumentContent> _contentToPost;

    // Outstanding POST requests
    private final Deque<Future<JsonValue>> _submitQueue = new LinkedList<>();

    // Document objects pending in the result queue
    private final Deque<Document> _resultQueue = new LinkedList<>();

    // Cap on the number of outstanding post batches
    private static final int SUBMIT_LIMIT = 10;

    // Target size (in bytes) for a document batch
    private static final long BATCH_UPLOAD_TARGET = 25000;
}
