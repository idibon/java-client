/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.IOException;

import javax.json.*;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Generates predictions for one or more predictable items.
 */
public class PredictionIterable<T extends Prediction> implements Iterable<T> {

    public Iterator<T> iterator() {
        return this.new Iter();
    }

    /**
     * Returns the key words and phrases from the document content that
     * affected the prediction.
     *
     * @return This
     */
    public PredictionIterable<T> withSignificantFeatures() {
        return withSignificantFeatures(0.7);
    }

    /**
     * Returns words and phrases from the document content that affected
     * the prediction above the provided threshold.
     *
     * @param threshold Defines the cutoff threshold to include features.
     *        Should be 0.0 - 1.0.
     * @return This
     */
    public PredictionIterable<T> withSignificantFeatures(double threshold) {
        _includeFeatures = true;
        _featureThreshold = threshold;
        return this;
    }

    PredictionIterable(Class<T> clazz, Task target,
                       Iterable<? extends DocumentContent> items) {
        _clazz = clazz;
        _target = target;
        _items = items;
    }

    // Include significant features with the results?
    private boolean _includeFeatures = false;

    // Cutoff threshold for feature significance
    private double _featureThreshold = 0.7;

    // The task being predicted against
    private final Task _target;

    // Type of predictions (span vs document) being performed
    private final Class<T> _clazz;

    // The items that will be predicted
    private final Iterable<? extends DocumentContent> _items;

    // Throttle the number of outstanding requests.
    private static final int DISPATCH_LIMIT = 10;

    /**
     * Iterates over predictable items and issues prediction API requests
     * for each.
     *
     * Uses a circular buffer internally (limited to DISPATCH_LIMIT items) to
     * store issued requests.
     */
    private class Iter implements Iterator<T> {
        private Iter() {
            _itemIt = _items.iterator();
            _queue = new LinkedList<>();
            advance(null);
        }

        public boolean hasNext() {
            return !_queue.isEmpty() || _itemIt.hasNext();
        }

        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            Entry head = _queue.removeFirst();

            JsonArray result = null;

            try {
                result = (JsonArray)head.future.get();
            } catch (ExecutionException ex) {
                throw new IterationException(_target.getEndpoint(),
                                             ex.getCause());
            } catch (InterruptedException | ClassCastException ex) {
                throw new IterationException(_target.getEndpoint(), ex);
            }

            try {
                T prediction = _clazz.newInstance();
                prediction.init((JsonArray)result, head.request, _target);
                advance(head);
                return prediction;
            } catch (InstantiationException | IllegalAccessException _) {
                throw new Error("Impossible");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advance(Entry last) {
            while (_queue.size() < DISPATCH_LIMIT && _itemIt.hasNext()) {
                Entry issue = (last != null) ? last : new Entry();
                issue.request = _itemIt.next();
                try {
                    issue.future = makePrediction(issue.request);
                } catch (IOException ex) {
                    throw new IterationException("Unable to predict " +
                                                 issue.request, ex);
                }
                _queue.addLast(issue);
                last = null;
            }
        }

        /**
         * Dispatch a prediction request.
         *
         * @param content The item to predict
         * @return A promise with the prediction result
         */
        private Future<JsonValue> makePrediction(DocumentContent content)
              throws IOException {
            JsonObjectBuilder bldr = JSON_BF.createObjectBuilder();
            JsonObject body = null;

            if (_includeFeatures) {
                bldr.add("features", true);
                bldr.add("feature_threshold", _featureThreshold);
            }

            if (content instanceof Document) {
                /* for Document objects in the same collection as the task,
                 * use in-place predictions to improve performance. this
                 * is restricted to Documents (not just DocumentContent.Named
                 * implementations) because the document must be physically
                 * present on the server to use this path. */
                Document doc = (Document)content;
                if (doc.getCollection().equals(_target.getCollection()))
                    body = bldr.add("document", doc.getName()).build();
            }

            if (body == null) {
                // fallback to ephemeral predictions
                bldr.add("content", content.getContent());
                JsonObject metadata = content.getMetadata();
                if (metadata != null) bldr.add("metadata", metadata);
                body = bldr.build();
            }

            return _target.getInterface().httpGet(_target.getEndpoint(), body);
        }

        private final Iterator<? extends DocumentContent> _itemIt;
        private final LinkedList<Entry> _queue;
    }

    private static class Entry {
        Future<JsonValue> future;
        DocumentContent request;
    }
}
