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

import javax.json.JsonValue;
import javax.json.JsonArray;

/**
 * Generates predictions for one or more predictable items.
 */
public class PredictionIterable<T extends Prediction> implements Iterable<T> {

    public Iterator<T> iterator() {
        return this.new Iter();
    }

    PredictionIterable(Class<T> clazz, Task target,
                       Iterable<? extends Predictable> items) {
        _clazz = clazz;
        _target = target;
        _items = items;
    }

    // The task being predicted against
    private final Task _target;

    // Type of predictions (span vs document) being performed
    private final Class<T> _clazz;

    // The items that will be predicted
    private final Iterable<? extends Predictable> _items;

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
            advance(null);
            _itemIt = _items.iterator();
            _queue = new LinkedList<>();
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
                T rv = _clazz.newInstance();
                rv.init((JsonArray)result, head.request);
                advance(head);
                return rv;
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
                issue.future = makePrediction(issue.request);
                _queue.addLast(issue);
                last = null;
            }
        }

        private Future<JsonValue> makePrediction(Predictable p) {
            try {
                return _target.getInterface()
                    .httpGet(_target.getEndpoint(), p.createPredictionRequest());
            } catch (IOException ex) {
                throw new IterationException(_target.getEndpoint(), ex);
            }
        }

        private final Iterator<? extends Predictable> _itemIt;
        private final LinkedList<Entry> _queue;
    }

    private static class Entry {
        Future<JsonValue> future;
        Predictable request;
    }
}
