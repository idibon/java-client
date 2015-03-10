/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

import com.idibon.api.http.*;
import javax.json.*;

import static com.idibon.api.model.Util.JSON_BF;

/**
 * The Collection is the top-most item in an analysis project.
 *
 * A Collection consists of Documents and Tasks.
 */
public class Collection extends IdibonHash {

    /**
     * Returns the raw JSON data for this Collection
     */
    @Override public JsonObject getJson() throws IOException {
        JsonObject collection = super.getJson(GET_FULL_DATA)
            .getJsonObject("collection");

        if (_cachedTasks == null) {
            // cache Task instances for every task in this collection
            Map<String, JsonObject> m = new LinkedHashMap<>();
            JsonArray tasks = collection.getJsonArray("tasks");
            if (tasks != null) {
                for (JsonObject t : tasks.getValuesAs(JsonObject.class))
                    m.put(t.getString("name"), t);
            }
            _cachedTasks = m;
        }
        return collection;
    }

    /**
     * Returns a DocumentSearcher instance to search for specific documents in
     * this collection.
     */
    public DocumentSearcher documents() {
        return new DocumentSearcher(this, _httpIntf);
    }

    /**
     * Returns a Document instance for a document with the given name.
     */
    public Document document(String name) {
        return Document.instance(this, name);
    }

    /**
     * Returns a Document instance for the provided JSON document hash
     */
    public Document document(JsonObject docJson) {
        return Document.instance(this, docJson);
    }

    /**
     * Uploads new content to the API
     *
     * @param documents The list of new documents that should be uploaded
     */
    public void addDocuments(Iterable<? extends DocumentContent> documents)
          throws IOException {
        addDocuments(documents.iterator());
    }

    /**
     * Uploads new content to the API
     *
     * @param documents The list of new documents that should be uploaded
     */
    public void addDocuments(Iterator<? extends DocumentContent> documents)
          throws IOException {
        try {
            PostDocumentsIterator uploader =
                new PostDocumentsIterator(this, documents);
            // consume the entire list to make sure everything has uploaded
            while (uploader.hasNext())
                uploader.next();
        } catch (IterationException ex) {
            /* Unwrap the iteration exception and re-throw the source, if
             * the reason was a protocol or IO exception */
            if (ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            else
                throw new IOException("Failed to upload docs", ex.getCause());
        }
    }

    /**
     * Adds or updates annotations in bulk on existing documents in this
     * collection.
     *
     * @param anns List of annotations to add
     */
    public void commitAnnotations(Iterable<? extends Annotation> anns)
          throws IOException {
        commitAnnotations(anns.iterator());
    }

    /**
     * Adds or updates annotations in bulk for existing documents in this
     * collection
     *
     * @param anns List of annotations to add
     */
    public void commitAnnotations(Iterator<? extends Annotation> anns)
          throws IOException {
        try {
            PostAnnotationsIterator uploader =
                new PostAnnotationsIterator(this, anns);
            while (uploader.hasNext())
                uploader.next();
        } catch (IterationException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException)ex.getCause();
            } else {
                throw new IOException("Failed to commit annotations",
                                      ex.getCause());
            }
        }

    }

    /**
     * Returns a Task instance for the named task.
     */
    public Task task(String name) {
        /* check if the task is already cached. store the _cachedTasks instance
         * var locally, in case a different thread invalidates the cached data
         * (nulling _cachedTasks) while this method is executing. */
        Map<String, JsonObject> cache = _cachedTasks;
        if (cache != null) {
            JsonObject hit = cache.get(name);
            if (hit != null) {
                return Task.instance(this,
                    JSON_BF.createObjectBuilder().add("task", hit).build());
            }
        }
        return Task.instance(this, name);
    }

    /**
     * Invalidate cached data.
     */
    @SuppressWarnings("unchecked")
    @Override public Collection invalidate() {
        super.invalidate();
        _cachedTasks = null;
        return this;
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Collection)) return false;

        Collection c = (Collection)other;
        return (c.getInterface() == getInterface() ||
                c.getInterface().equals(getInterface())) &&
            c.getEndpoint().equals(getEndpoint());
    }

    @Override public int hashCode() {
        return getEndpoint().hashCode();
    }

    /**
     * Returns a Collection instance for the provided collection name
     *
     * @param httpIntf The HTTP interface to use to access the Collection
     * @param name The name of the collection
     */
    public static Collection instance(HttpInterface httpIntf, String name) {
        return new Collection(httpIntf, name);
    }

    private Collection(HttpInterface httpIntf, String name) {
        super("/" + percentEncode(name), httpIntf);
        _name = name;
    }

    // cache of task data for quick lookup
    private volatile Map<String, JsonObject> _cachedTasks;

    // The name of the collection (un-escaped)
    private final String _name;

    // Query used to return full task and label data from getJson()
    private static final JsonObject GET_FULL_DATA =
        JSON_BF.createObjectBuilder().add("full", true).build();
}
