/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Arrays;

import com.idibon.api.http.*;
import com.google.gson.*;

/**
 * Class for searching for Documents in a Collection by text, date,
 * annotation features, or other aspects, and then iterating over the
 * results.
 */
public class DocumentSearcher implements Iterable<Document> {

    /**
     * Types of data that can be returned in various search
     * modes. See {@link com.idibon.api.model.DocumentSearcher#returning}
     */
    public enum ReturnData {
        /**
         * include annotations for just the searched-for tasks
         */
        TaskAnnotations,
        /**
         * include annotations for all tasks
         */
        AllAnnotations,
        /**
         * include the document features for the searched task
         */
        TaskFeatures,
        /**
         * return the tokenized document content
         */
        DocumentTokens,
        /**
         * return the document content and metadata
         */
        DocumentContent;
    }

    /**
     * Configure the search iterator to include additional return data.
     *
     * By default, only the Document name will be returned from the
     * server. If needed, additional data may be returned.
     *
     * Depending on the data requested, this may invalidate certain search
     * capabilities.
     *
     * @param returnValues Array of ReturnData enumerants of data that
     *        should be included.
     */
    public DocumentSearcher returning(ReturnData... returnValues) {
        for (ReturnData r : returnValues)
            _returns[r.ordinal()] = true;
        return this;
    }

    /**
     * Excludes the specified extra return data from the search results.
     *
     * See {@link com.idibon.api.model.DocumentSearcher#returning}.
     *
     * @param excludeValues Array of ReturnData enumerants of data that
     *        do not need to be returned.
     */
    public DocumentSearcher excluding(ReturnData... excludeValues) {
        for (ReturnData r : excludeValues)
            _returns[r.ordinal()] = false;
        return this;
    }

    /**
     * Searches for documents that include the provided text string in
     * the content field.
     *
     * This search feature is only supported when the requested return
     * data is either empty, or only DocumentContent.
     *
     * @param text String to use for content matching.
     */
    public DocumentSearcher thatIncludes(String text) {
        _contentFilter = (text != null && text.isEmpty()) ? null : text;
        return this;
    }

    /**
     * Defines a task filter that searches only for documents that have
     * annotations for the specified task(s). Use an empty list to return
     * all documents, even those that have no annotations.
     *
     * Disables a label filter, if one was previously assigned.
     *
     * @param tasks List of task names
     */
    public DocumentSearcher annotatedForTasks(String... tasks) {
        _taskFilter = (tasks.length == 0) ?
            null : Arrays.copyOf(tasks, tasks.length);
        _labelFilter = null;
        return this;
    }

    /**
     * Defines a search filter that will return documents that have annotations
     * for the specified label(s) in the named task.
     */
    public DocumentSearcher annotatedForLabels(String task, String... labels) {
        if (labels.length == 0) unsupported("Empty labels array");
        if (task == null) throw new NullPointerException("task");

        _taskFilter = new String[] { task };
        _labelFilter = Arrays.copyOf(labels, labels.length);
        return this;
    }

    /**
     * Specifies a task whose features should be used as the return data
     * when TaskFeatures are included with the return data.
     *
     * When TaskFeatures are returned and a task filter has been specified,
     * the return data will include the features from the first task
     * specified. However, if no task filter is used, or features from a
     * different task are desired, use this method to define the feature
     * generator.
     *
     * @param task Name of a task
     */
    public DocumentSearcher usingFeaturesFrom(String task) {
        _taskFeatureGen = task;
        return this;
    }

    /**
     * Validates the current search configuration, sends the request to
     * the server, and returns the results of the query.
     */
    public Iterator<Document> iterator() {
        validate();
        return this.new Iter();
    }

    DocumentSearcher(Collection collection, HttpInterface httpIntf) {
        _collection = collection;
        _httpIntf = httpIntf;
    }

    /**
     * Returns true if the requested return data requires the use of document
     * streaming mode.
     */
    private boolean needsStreamingMode() {
        return _returns[ReturnData.TaskAnnotations.ordinal()] ||
            _returns[ReturnData.AllAnnotations.ordinal()] ||
            _returns[ReturnData.TaskFeatures.ordinal()] ||
            _returns[ReturnData.DocumentTokens.ordinal()];
    }

    /**
     * RSI-reducer
     */
    private void unsupported(String desc) {
        throw new UnsupportedOperationException(desc);
    }

    /**
     * Validates that the search terms are in a consistent state, and
     * supported by the API.
     */
    private void validate() {
        if (_returns[ReturnData.TaskAnnotations.ordinal()] &&
                _taskFilter == null &&
                !_returns[ReturnData.AllAnnotations.ordinal()]) {
            unsupported("Task list required to return TaskAnnotations");
        }

        if (needsStreamingMode() && _contentFilter != null)
            unsupported("Full-text search incompatible with return data");

        if (_returns[ReturnData.TaskFeatures.ordinal()]) {
            if (_taskFilter == null && _taskFeatureGen == null)
                unsupported("A task must be provided to return TaskFeatures");
        }
    }

    /**
     * Returns true if the requested return data requires the use of document
     * full-content mode.
     */
    private boolean needsFullContentMode() {
        return _returns[ReturnData.DocumentContent.ordinal()];
    }

    /**
     * Simple helper method to convert an array of java strings into a GSON
     * JsonElement that can be added as a key-value pair in a JsonObject
     */
    private static JsonArray toJsonArray(String[] javaArray) {
        JsonArray result = new JsonArray();
        for (String s : javaArray) result.add(new JsonPrimitive(s));
        return result;
    }

    /// Escaped HTTP endpoint for the Collection that created this iterator
    private final Collection _collection;

    /// HTTP interface to use to perform the query
    private final HttpInterface _httpIntf;

    /// Return data configuration. Array of booleans with 1 entry for each datum
    private final boolean[] _returns = new boolean[ReturnData.values().length];

    /// Configured task annotation filters, if any.
    private String[] _taskFilter;

    /// Configured label annotation filters, if any.
    private String[] _labelFilter;

    /// Configured full-text search filter, if any.
    private String _contentFilter;

    /// Specific generator task for returned features
    private String _taskFeatureGen;

    /**
     * Inner class responsible for paging through HTTP results and converting
     * the results into Document instances.
     */
    private class Iter implements Iterator<Document> {

        private Iter() {
            /* cache the streaming mode, since the format of the returned
             * JSON elements will be different */
            _streaming = needsStreamingMode();
            if (_streaming) {
                _query.addProperty("stream", true);
                _query.add("doc_args", requestStreamingReturnData());
            } else if (needsFullContentMode()) {
                _query.addProperty("full", true);
            }

            if (_contentFilter != null)
                _query.addProperty("text", _contentFilter);

            if (_taskFilter != null)
                _query.add("task", toJsonArray(_taskFilter));

            if (_labelFilter != null)
                _query.add("label", toJsonArray(_labelFilter));

            dispatchNext(null);
        }

        /**
         * Returns the next Document from the search results.
         */
        public Document next() {
            if (!hasNext())
                throw new NoSuchElementException();

            if (_currentBatch == null || _offset >= _currentBatch.size()) {
                try {
                    waitForNextBatch();
                    _offset = 0;
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof IOException)
                        throw new IterationException(_endpoint, ex.getCause());
                    else
                        throw new IterationException(_endpoint, ex);
                } catch (InterruptedException ex) {
                    throw new IterationException(_endpoint, ex);
                }
            }

            if (_currentBatch == null || _offset >= _currentBatch.size())
                throw new NoSuchElementException();

            JsonObject entry = _currentBatch.get(_offset).getAsJsonObject();
            _offset += 1;
            if (_streaming) {
                String name = entry.getAsJsonObject("document")
                    .getAsJsonPrimitive("name").getAsString();
                return new Document(name, _collection, _httpIntf).preload(entry);
            } else {
                String name = entry.getAsJsonPrimitive("name").getAsString();
                /* preload the returned Document object with whatever data
                 * was requested. */
                JsonObject data = new JsonObject();
                data.add("document", entry);
                return new Document(name, _collection, _httpIntf).preload(data);
            }
        }

        /**
         * Returns true if there is at least one more Document in the search
         * results.
         */
        public boolean hasNext() {
            return _nextBatch != null || _offset < _currentBatch.size();
        }

        /**
         * Unimplemented.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Formats the requested return data items into a JSON hash to transmit
         * to the API. Used by constructor.
         */
        private JsonObject requestStreamingReturnData() {
            JsonObject args = new JsonObject();
            args.addProperty("skip_null_fields", true); // never return nulls
            if (_returns[ReturnData.DocumentTokens.ordinal()])
                args.addProperty("tokens", true);

            if (_returns[ReturnData.AllAnnotations.ordinal()]) {
                // nothing to do, this is the default behavior
            } else if (_returns[ReturnData.TaskAnnotations.ordinal()]) {
                // provide all of the tasks that were included in the filter
                args.add("task", toJsonArray(_taskFilter));
            } else {
                // no annotations wanted, skip over them
                args.addProperty("skip_annotations", true);
            }

            /* always use compact wire format for tokens, annotations and
             * features */
            args.addProperty("format", "compact");

            if (_returns[ReturnData.TaskFeatures.ordinal()]) {
                String task = _taskFeatureGen != null ?
                    _taskFeatureGen : _taskFilter[0];
                args.addProperty("features", task);
            }
            return args;
        }

        /**
         * Waits for a dispatched batch to complete and normalizes the returned
         * JSON objects into a common format. Used by #next().
         */
        private void waitForNextBatch()
            throws ExecutionException, InterruptedException {
            JsonElement cursor = null;

            Future<JsonElement> async = _nextBatch;
            _nextBatch = null;

            if (_streaming) {
                _currentBatch = async.get().getAsJsonArray();
                if (_currentBatch.size() == 0)
                    return;
                cursor = _currentBatch.get(_currentBatch.size() - 1)
                    .getAsJsonObject().get("cursor");
            } else {
                JsonObject rv = async.get().getAsJsonObject();
                _currentBatch = rv.getAsJsonArray("documents");
                cursor = rv.get("cursor");
            }

            // pre-load the next batch if one exists
            if (cursor != null && cursor.isJsonPrimitive())
                dispatchNext(cursor.getAsString());
        }

        /**
         * Issues an HTTP request for the next page of HTTP results. Used by
         * #waitForNextBatch().
         */
        private void dispatchNext(String cursor) {
            if (cursor == null)
                _query.remove("cursor");
            else
                _query.addProperty("cursor", cursor);
            try {
                _nextBatch = _httpIntf.httpGet(_endpoint, _query);
            } catch (IOException ex) {
                throw new IterationException(_endpoint, ex);
            }
        }

        /// The endpoint used for document iteration
        private final String _endpoint = _collection.getEndpoint() + "/*";

        /// The search query used for this document iteration
        private final JsonObject _query = new JsonObject();

        /// If streaming mode is used
        private final boolean _streaming;

        private Future<JsonElement> _nextBatch;
        private JsonArray _currentBatch;
        private int _offset;
    }
}
