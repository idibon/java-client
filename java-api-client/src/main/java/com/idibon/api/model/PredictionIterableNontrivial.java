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
import com.idibon.api.http.HttpInterface;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * Generates predictions for one or more predictable items.
 */
class PredictionIterableNontrivial<T extends Prediction<T>>
      implements PredictionIterable<T>{

    /**
     * Default feature threshold. Returns moderate-strongly predictive features
     */
    public static final double DEFAULT_FEATURE_THRESHOLD = 0.7;

    public Iterator<Either<APIFailure<DocumentContent>, T>> iterator() {
        return this.new Iter();
    }

    /**
     * Returns the key words and phrases from the document content that
     * affected the prediction.
     *
     * This is the same as calling
     * {@link com.idibon.api.model.PredictionIterableNontrivial#withSignificantFeatures(double)}
     * with a value of DEFAULT_FEATURE_THRESHOLD.
     *
     * @return This
     */
    public PredictionIterableNontrivial<T> withSignificantFeatures() {
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
    public PredictionIterableNontrivial<T> withSignificantFeatures(double threshold) {
        _includeFeatures = true;
        _featureThreshold = threshold;
        return this;
    }

    PredictionIterableNontrivial(Class<T> clazz, Task target,
          Iterable<? extends DocumentContent> items) {
        try {
            _constructor = clazz.getDeclaredConstructor(
                JsonArray.class, DocumentContent.class, Task.class);
        } catch (Exception ex) {
            throw new Error("Impossible");
        }

        /* configure the size of the internal request circular buffer based
         * on the number of parallel requests supported by the HTTP interface.
         * predictions should saturate this interface. double-buffer to ensure
         * that OS thread-scheduling doesn't cause a thread to go idle waiting
         * for the dispatch thread to add more work */
        _dispatchLimit = 2 * target.getInterface().getProperty(HttpInterface.Property.ParallelRequestLimit,
            DEFAULT_DISPATCH_LIMIT);

        /* disable hierarchical predictions, since these don't work very well
         * setting the threshold to a value above 1.1 ensures that the server
         * will never traverse down the hierarchy, since the maximum confidence
         * for any prediction is 1.0. */
        if (DocumentPrediction.class.isAssignableFrom(clazz))
            _predictionThreshold = 1.1;
        _target = target;
        _items = items;
    }

    // Include significant features with the results?
    private boolean _includeFeatures = false;

    // Cutoff threshold for feature significance
    private double _featureThreshold = DEFAULT_FEATURE_THRESHOLD;

    /* Cutoff threshold for returned spans / document hierarchies (0.49 is
     * the server's default value */
    private double _predictionThreshold = 0.49;

    // The task being predicted against
    private final Task _target;

    /* The maximum number of outstanding requests to issue, based on the
     * HTTP interface parallelism limit */
    private final int _dispatchLimit;

    // Type of predictions (span vs document) being performed
    private final Constructor<T> _constructor;

    // The items that will be predicted
    private final Iterable<? extends DocumentContent> _items;

    // Default throttle for the number of outstanding requests.
    private static final int DEFAULT_DISPATCH_LIMIT = 10;

    /**
     * Iterates over predictable items and issues prediction API requests
     * for each.
     *
     * Uses a circular buffer internally (limited to _dispatchLimit items) to
     * store issued requests.
     */
    private class Iter
          implements Iterator<Either<APIFailure<DocumentContent>, T>> {
        private Iter() {
            _itemIt = _items.iterator();
            _queue = new LinkedList<>();
            advance(null);
        }

        public boolean hasNext() {
            return !_queue.isEmpty() || _itemIt.hasNext();
        }

        public Either<APIFailure<DocumentContent>, T> next() {
            if (!hasNext()) throw new NoSuchElementException();
            Entry head = _queue.removeFirst();

            Either<IOException, JsonArray> result =
                head.future.getAs(JsonArray.class);

            if (result.isLeft()) {
                return Either.left(
                    APIFailure.failure(result.left, head.request));
            }

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
            while (_queue.size() < _dispatchLimit && _itemIt.hasNext()) {
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
            JsonObjectBuilder bldr = JSON_BF.createObjectBuilder()
                .add("threshold", _predictionThreshold);
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
