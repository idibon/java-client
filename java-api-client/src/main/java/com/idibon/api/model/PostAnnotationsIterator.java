/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;

import com.idibon.api.http.HttpFuture;
import com.idibon.api.util.Either;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Utility class to batch and post annotation updates to one or more documents.
 */
class PostAnnotationsIterator
      implements Iterator<Either<APIFailure<List<Annotation>>, Void>> {

    public boolean hasNext() {
        return (!_quit && _annotations.hasNext()) ||
            !_submit.isEmpty() ||
            !_complete.isEmpty();
    }

    public Either<APIFailure<List<Annotation>>, Void> next() {
        /* make sure that at least one batch of work has been submitted */
        advance();
        /* if a result is ready, return it */
        Either<APIFailure<List<Annotation>>, Void> head = _complete.pollFirst();

        if (head == null) {
            /* otherwise, wait for the oldest batch to complete, and
             * return that */
            Request next = _submit.peekFirst();
            if (next == null) throw new NoSuchElementException();
            // wait for the next item to complete
            next.future.get();
            advance();
            head = _complete.pollFirst();
            if (head == null) throw new NoSuchElementException();
        }
        /* and since at least one item has been completed, submit as
         * much work as we can. */
        advance();
        return head;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Consumes any already-complete futures so that the issue slots are
     * available, and then issues more batches to fill avaialable slots.
     */
    private void advance() {
        // grab all of the completed items off of the pending queue
        for (Request head = _submit.peekFirst();
                 head != null && head.future.isDone();
                 head = _submit.peekFirst()) {
            _submit.removeFirst();
            Either<IOException, JsonValue> result = head.future.get();
            if (result.isLeft()) {
                APIFailure<List<Annotation>> err =
                    APIFailure.failure(result.left, head.batch);
                _complete.add(
                    Either.<APIFailure<List<Annotation>>, Void>left(err)
                );
            } else {
                _complete.add(
                    Either.<APIFailure<List<Annotation>>, Void>right(null)
                );
            }
        }

        while (_annotations.hasNext() && !_quit &&
                 _submit.size() < SUBMIT_LIMIT) {
            submitNextBatch();
        }
    }

    /**
     * Iterates over the annotations and creates a JSON payload with up to
     * BATCH_LIMIT annotation updates across any number of documents, and
     * asynchronously submits the request.
     *
     * @return A Future representing the completion of the batch.
     */
    private void submitNextBatch() {
        Map<String, JsonArrayBuilder> batch = new HashMap<>();
        Request req = new Request();

        // a pseudo-request in case a failure happens in JSON serialization
        Request failure = null;

        while (_annotations.hasNext() && !_quit && failure == null &&
                 req.batch.size() < BATCH_LIMIT) {
            Annotation ann = _annotations.next();
            Document doc = getTargetDocument(ann);
            JsonArrayBuilder documentAnns = batch.get(doc.getName());
            if (documentAnns == null) {
                documentAnns = JSON_BF.createArrayBuilder();
                batch.put(doc.getName(), documentAnns);
            }
            try {
                documentAnns.add(Util.toJson(ann));
                req.batch.add(ann);
            } catch (IOException ex) {
                failure = new Request();
                failure.batch.add(ann);
                failure.future = HttpFuture.wrap(HttpIssueError.wrap(ex));
            }
        }

        if (!req.batch.isEmpty()) {
            JsonArrayBuilder batchJson = JSON_BF.createArrayBuilder();
            for (Map.Entry<String, JsonArrayBuilder> entry : batch.entrySet()) {
                /* create a JSON hash { name: $name, annotations: $annotations }
                 * for each document in the map, and add it to the json batch */
                JsonArray documentBatch = entry.getValue().build();
                batchJson.add(JSON_BF.createObjectBuilder()
                    .add(Document.Keys.name.name(), entry.getKey())
                    .add(Document.Keys.annotations.name(), documentBatch)
                    .build());
            }

            String endpoint = _collection.getEndpoint() + "/*";

            // add the array to a hash { documents: $array } and POST it
            req.future = _collection.getInterface().httpPost(endpoint,
                JSON_BF.createObjectBuilder()
                .add("documents", batchJson.build())
                .build());
            _submit.add(req);
        }

        if (failure != null) {
            _submit.add(failure);
            _quit = _stopOnError;
        }
    }

    /**
     * Verifies that the annotation is appropriate for a batch update. Ensures
     * that the document exists on the server, and that the operation is
     * being performed on the same collection as where the document is stored.
     *
     * @param ann The annotation to validate.
     * @return The document that the annotation is updating
     */
    private Document getTargetDocument(Annotation ann) {
        Document doc = (ann instanceof Annotation.Judgment)
            ? ((Annotation.Judgment)ann).assignment.document
            : ((Annotation.Assignment)ann).document;

        if (doc == null)
            throw new IllegalStateException("Invalid document");

        if (!doc.getCollection().equals(_collection))
            throw new IllegalArgumentException("Invalid collection");

        return doc;
    }

    PostAnnotationsIterator(Collection collection,
          Iterator<? extends Annotation> annotations,
          boolean stopOnError) {
        _annotations = annotations;
        _collection = collection;
        _stopOnError = stopOnError;
    }

    private final LinkedList<Request> _submit =
        new LinkedList<>();
    private final LinkedList<Either<APIFailure<List<Annotation>>, Void>>
        _complete = new LinkedList<>();
    private final Iterator<? extends Annotation> _annotations;
    private final Collection _collection;
    private final boolean _stopOnError;
    private boolean _quit;

    /* Batching annotation updates into groups of 40 seems to work well in
     * practice, and dramatically reduces network overhead */
    private static final int BATCH_LIMIT = 40;

    /* Annotation updates are pretty heavy-weight API operations, so limit
     * the total number in-flight to ~100. */
    private static final int SUBMIT_LIMIT = 3;

    /**
     * Union type of the annotations submitted in the HTTP request and the
     * promised result.
     */
    private static class Request {
        HttpFuture<JsonValue> future;
        List<Annotation> batch = new ArrayList<>();
    }
}
