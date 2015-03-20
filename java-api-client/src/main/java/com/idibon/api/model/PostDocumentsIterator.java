/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;

import com.idibon.api.util.Either;
import com.idibon.api.http.HttpFuture;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Background batch document upload process. Runs independently until
 * completion or an error occurs.
 */
class PostDocumentsIterator
      implements Iterator<Either<IOException, Document>> {

    public boolean hasNext() {
        return _contentToPost.hasNext() ||
            !_submitQueue.isEmpty() ||
            !_resultQueue.isEmpty();
    }

    public Either<IOException, Document> next() {
        Either<IOException, Document> nextDoc = _resultQueue.pollFirst();
        if (nextDoc == null) {
            waitForNextBatch();
            nextDoc = _resultQueue.pollFirst();
            if (nextDoc == null)
                throw new NoSuchElementException("No more documents");
        }
        try {
            advance();
        } catch (IOException ex) {
            /* add the error to the submit queue, so it will be returned to
             * the caller in issue-order */
            _submitQueue.add(HttpFuture.wrap(HttpIssueError.wrap(ex)));
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
        for (HttpFuture<JsonValue> head = _submitQueue.peek();
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
        Either<IOException, JsonObject> result =
            _submitQueue.removeFirst().getAs(JsonObject.class);

        if (result.isLeft()) {
            // FIXME: add this for each document?
            _resultQueue.add(Either.<IOException, Document>left(result.left));
        } else {
            JsonArray documents = result.right.getJsonArray("documents");
            if (documents == null) {
                _resultQueue.add(Either.<IOException, Document>
                                 left(new IOException("No data")));
            } else {
                for (JsonObject doc : documents.getValuesAs(JsonObject.class)) {
                    _resultQueue.add(Either.<IOException, Document>right(
                        _collection.document(doc.getString("name")))
                    );
                }
            }
        }
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
    private final Deque<HttpFuture<JsonValue>> _submitQueue =
        new LinkedList<>();

    // Document objects pending in the result queue
    private final Deque<Either<IOException, Document>> _resultQueue =
        new LinkedList<>();

    // Cap on the number of outstanding post batches
    private static final int SUBMIT_LIMIT = 10;

    // Target size (in bytes) for a document batch
    private static final long BATCH_UPLOAD_TARGET = 25000;
}
