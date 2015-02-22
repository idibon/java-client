/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import com.idibon.api.http.*;
import javax.json.JsonObject;

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
        return super.getJson().getJsonObject("collection");
    }

    /**
     * Returns a DocumentSearcher instance to search for specific documents in
     * this collection.
     */
    public DocumentSearcher documents() {
        return new DocumentSearcher(this, _httpIntf);
    }

    /**
     * Returns a Document instnace for the provided JSON document hash
     */
    public Document document(JsonObject docJson) {
        return Document.instance(this, docJson);
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

    /// The name of the collection (un-escaped)
    private final String _name;
}
