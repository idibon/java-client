/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;

import com.idibon.api.util.Either;
import com.idibon.api.http.HttpFuture;
import com.idibon.api.http.HttpInterface;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Background batch document upload process. Runs independently until
 * completion or an error occurs.
 */
class PostDocumentsIterator
      implements Iterator<Either<APIFailure<List<DocumentContent>>, Document>> {

    public boolean hasNext() {
        return (_contentToPost.hasNext() && !_quit)||
            !_submitQueue.isEmpty() ||
            !_resultQueue.isEmpty();
    }

    public Either<APIFailure<List<DocumentContent>>, Document> next() {
        Either<APIFailure<List<DocumentContent>>, Document> nextDoc =
            _resultQueue.pollFirst();

        if (nextDoc == null) {
            waitForNextBatch();
            nextDoc = _resultQueue.pollFirst();
            if (nextDoc == null)
                throw new NoSuchElementException("No more documents");
        }
        advance();
        return nextDoc;
    }

    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Consume any completed batch uploads, then dispatch more if free
     * slots are available.
     */
    void advance() {
        /* search for any complete results, grab the results, and dispatch
         * additional requests */
        for (Request head = _submitQueue.peek();
               head != null && head.future.isDone();
               head = _submitQueue.peek()) {
            waitForNextBatch(); // no wait actually needed
        }

        long estimatedSize = 0;
        JsonArrayBuilder batch = null;
        List<DocumentContent> rawBatch = null;

        /* For performance, upload documents in batches, and upload multiple
         * batches in parallel. Empirically, the best performance seems to be
         * when the upload batch size 25KiB - 100KiB. */
        while (_submitQueue.size() < _submitLimit &&
               !_quit && _contentToPost.hasNext()) {
            // create a new array for this batch of documents, if needed
            if (batch == null) batch = JSON_BF.createArrayBuilder();
            if (rawBatch == null) rawBatch = new ArrayList<>();

            DocumentContent item = _contentToPost.next();

            try {
                JsonObject object = Util.toJson(item);
                batch.add(object);
                rawBatch.add(item);
                estimatedSize += Util.estimateSizeOfJson(object);
            } catch (IOException ex) {
                // submit any documents already converted
                submitBatch(rawBatch, batch);
                // submit a pseudo-batch for the failed document
                Request broken = new Request();
                broken.batch = Arrays.asList(item);
                broken.future = HttpFuture.wrap(HttpIssueError.wrap(ex));
                _submitQueue.add(broken);
                // mark the quit flag if stop on error is true
                _quit = _stopOnError;
                if (!_quit) {
                    // reset for the next batch if upload will continue
                    rawBatch.clear();
                    batch = JSON_BF.createArrayBuilder();
                }
            }

            if (estimatedSize >= BATCH_UPLOAD_TARGET) {
                submitBatch(rawBatch, batch);
                batch = JSON_BF.createArrayBuilder();
                rawBatch.clear();
                estimatedSize = 0;
            }
        }

        // submit the last partial batch, if one exists
        submitBatch(rawBatch, batch);
    }

    /**
     * Waits for the next batch upload to complete and parse the results.
     */
    private void waitForNextBatch() {
        // throw an exception if there isn't a batch
        Request request = _submitQueue.removeFirst();

        Either<IOException, JsonObject> result =
            request.future.getAs(JsonObject.class);

        if (result.isLeft()) {
            APIFailure<List<DocumentContent>> err =
                APIFailure.failure(result.left, request.batch);
            _resultQueue.add(
                Either.<APIFailure<List<DocumentContent>>, Document>left(err)
             );
            _quit = _stopOnError;
        } else {
            JsonArray documents = result.right.getJsonArray("documents");
            for (JsonObject doc : documents.getValuesAs(JsonObject.class)) {
                _resultQueue.add(
                    Either.<APIFailure<List<DocumentContent>>, Document>right(
                        _collection.document(doc.getString("name"))
                    ));
            }
        }
    }

    /**
     * POST a batch of documents to the API.
     */
    private void submitBatch(List<DocumentContent> rawBatch,
          JsonArrayBuilder batch) {
        if (rawBatch == null || rawBatch.isEmpty())
            return;
        JsonObject body = JSON_BF.createObjectBuilder()
            .add("documents", batch)
            .build();

        String ep = _collection.getEndpoint() + "/*";
        Request req = new Request();
        req.future = _collection.getInterface().httpPost(ep, body);
        req.batch = rawBatch;
        _submitQueue.add(req);
    }

    PostDocumentsIterator(Collection collection,
          Iterator<? extends DocumentContent> contentToPost,
          boolean stopOnError) throws IOException {
        _contentToPost = contentToPost;
        _collection = collection;
        _stopOnError = stopOnError;
        /* Limit to at most MAXIMUM_SUBMIT_LIMIT parallel upload requests,
         * regardless of connection parallelism, to prevent over-committing
         * the API */
        _submitLimit = Math.min(MAXIMUM_SUBMIT_LIMIT,
            collection.getInterface()
                      .getProperty(HttpInterface.Property.ParallelRequestLimit,
                                   DEFAULT_SUBMIT_LIMIT));
        advance();
    }

    // Terminates iteration prematurely
    private boolean _quit;

    // Collection being posted to
    private final Collection _collection;

    // Content to post
    private final Iterator<? extends DocumentContent> _contentToPost;

    // Stops submitting more batches following an error
    private final boolean _stopOnError;

    // Limit to the number of outstanding batches uploaded at once
    private final int _submitLimit;

    // Outstanding POST requests
    private final Deque<Request> _submitQueue = new LinkedList<>();

    // Document objects pending in the result queue
    private final Deque<Either<APIFailure<List<DocumentContent>>, Document>>
        _resultQueue = new LinkedList<>();

    // Default limit to the number of outstanding post batches
    private static final int DEFAULT_SUBMIT_LIMIT = 10;

    // Maximum number of parallel batch uploads
    private static final int MAXIMUM_SUBMIT_LIMIT = 25;

    // Target size (in bytes) for a document batch
    private static final long BATCH_UPLOAD_TARGET = 25000;

    /**
     * Simple tuple storing the requested batch and the promised result.
     */
    private static class Request {
        HttpFuture<JsonValue> future;
        List<DocumentContent> batch;
    }
}
