/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.IOException;

import javax.json.*;

import com.idibon.api.util.Either;
import com.idibon.api.http.HttpFuture;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Generates predictions for one or more predictable items.
 */
public class PredictionIterable<T extends Prediction>
      implements Iterable<Either<IOException, T>> {

    /**
     * Default feature threshold. Returns moderate-strongly predictive features
     */
    public static final double DEFAULT_FEATURE_THRESHOLD = 0.7;

    public Iterator<Either<IOException, T>> iterator() {
        return this.new Iter();
    }

    /**
     * Returns the key words and phrases from the document content that
     * affected the prediction.
     *
     * This is the same as calling
     * {@link com.idibon.api.model.PredictionIterable#withSignificantFeatures(double)}
     * with a value of DEFAULT_FEATURE_THRESHOLD.
     *
     * @return This
     */
    public PredictionIterable<T> withSignificantFeatures() {
        return withSignificantFeatures(DEFAULT_FEATURE_THRESHOLD);
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
        try {
            _constructor = clazz.getDeclaredConstructor(
                JsonArray.class, DocumentContent.class, Task.class);
        } catch (Exception ex) {
            throw new Error("Impossible");
        }
        _target = target;
        _items = items;
    }

    // Include significant features with the results?
    private boolean _includeFeatures = false;

    // Cutoff threshold for feature significance
    private double _featureThreshold = DEFAULT_FEATURE_THRESHOLD;

    // The task being predicted against
    private final Task _target;

    // Type of predictions (span vs document) being performed
    private final Constructor<T> _constructor;

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
    private class Iter implements Iterator<Either<IOException, T>> {
        private Iter() {
            _itemIt = _items.iterator();
            _queue = new LinkedList<>();
            advance(null);
        }

        public boolean hasNext() {
            return !_queue.isEmpty() || _itemIt.hasNext();
        }

        public Either<IOException, T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            Entry head = _queue.removeFirst();

            Either<IOException, JsonArray> result =
                head.future.getAs(JsonArray.class);

            if (result.isLeft()) return Either.left(result.left);

            try {
                T prediction = _constructor.newInstance(
                    result.right, head.request, _target);
                advance(head);
                return Either.right(prediction);
            } catch (InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException _) {
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
                issue.future = makePrediction(issue.request);
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
        private HttpFuture<JsonValue> makePrediction(DocumentContent content) {
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
                try {
                    bldr.add("content", content.getContent());
                    JsonObject metadata = content.getMetadata();
                    if (metadata != null) bldr.add("metadata", metadata);
                    body = bldr.build();
                } catch (IOException ex) {
                    return HttpFuture.wrap(HttpIssueError.wrap(ex));
                }
            }

            return _target.getInterface().httpGet(_target.getEndpoint(), body);
        }

        private final Iterator<? extends DocumentContent> _itemIt;
        private final LinkedList<Entry> _queue;
    }

    private static class Entry {
        HttpFuture<JsonValue> future;
        DocumentContent request;
    }
}
