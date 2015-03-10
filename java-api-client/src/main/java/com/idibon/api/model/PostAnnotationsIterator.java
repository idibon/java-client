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
 * Utility class to batch and post annotation updates to one or more documents.
 */
class PostAnnotationsIterator implements Iterator<JsonValue> {

    public boolean hasNext() {
        return _annotations.hasNext() ||
            !_submit.isEmpty() ||
            !_complete.isEmpty();
    }

    public JsonValue next() {
        try {
            advance();
            JsonValue head = _complete.pollFirst();
            if (head == null) {
                Future<JsonValue> next = _submit.pollFirst();
                if (next == null) throw new NoSuchElementException();
                head = next.get();
            }
            advance();
            return head;
        } catch (ExecutionException ex) {
            throw new IterationException("Wrapped exception", ex.getCause());
        } catch (InterruptedException|IOException ex) {
            throw new IterationException("Wrapped exception", ex);
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Consumes any already-complete futures so that the issue slots are
     * available, and then issues more batches to fill avaialable slots.
     */
    private void advance()
          throws IOException, ExecutionException, InterruptedException {
        // grab all of the completed items off of the pending queue
        for (Future<JsonValue> head = _submit.peekFirst();
                 head != null && head.isDone();
                 head = _submit.peekFirst()) {
            _submit.removeFirst();
            _complete.add(head.get());
        }

        while (_annotations.hasNext() && _submit.size() < SUBMIT_LIMIT)
            _submit.add(submitNextBatch());
    }

    /**
     * Iterates over the annotations and creates a JSON payload with up to
     * BATCH_LIMIT annotation updates across any number of documents, and
     * asynchronously submits the request.
     *
     * @return A Future representing the completion of the batch.
     */
    private Future<JsonValue> submitNextBatch() throws IOException {
        Map<String, JsonArrayBuilder> batch = new HashMap<>();
        int batchSize = 0;

        while (_annotations.hasNext() && batchSize < BATCH_LIMIT) {
            Annotation ann = _annotations.next();
            Document doc = getTargetDocument(ann);
            JsonArrayBuilder documentAnns = batch.get(doc.getName());
            if (documentAnns == null) {
                documentAnns = JSON_BF.createArrayBuilder();
                batch.put(doc.getName(), documentAnns);
            }
            documentAnns.add(Util.toJson(ann));
        }

        JsonArrayBuilder batchJson = JSON_BF.createArrayBuilder();
        for (Map.Entry<String, JsonArrayBuilder> entry : batch.entrySet()) {
            /* create a JSON hash { name: $name, annotations: $annotations }
             * for each document in the map, and add it to the array batch */
            batchJson.add(JSON_BF.createObjectBuilder()
                .add(Document.Keys.name.name(), entry.getKey())
                .add(Document.Keys.annotations.name(), entry.getValue().build())
                .build());
        }

        String endpoint = _collection.getEndpoint() + "/*";

        // add the array to a hash { documents: $array } and POST it
        return _collection.getInterface().httpPost(endpoint,
            JSON_BF.createObjectBuilder()
            .add("documents", batchJson.build())
            .build());
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
          Iterator<? extends Annotation> annotations) {
        _annotations = annotations;
        _collection = collection;
    }

    private final LinkedList<Future<JsonValue>> _submit = new LinkedList<>();
    private final LinkedList<JsonValue> _complete = new LinkedList<>();
    private final Iterator<? extends Annotation> _annotations;
    private final Collection _collection;

    /* Batching annotation updates into groups of 40 seems to work well in
     * practice, and dramatically reduces network overhead */
    private static final int BATCH_LIMIT = 40;

    /* Annotation updates are pretty heavy-weight API operations, so limit
     * the total number in-flight to ~100. */
    private static final int SUBMIT_LIMIT = 3;
}
