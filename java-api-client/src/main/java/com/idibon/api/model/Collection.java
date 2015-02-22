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
     * Constructor for Collections
     *
     * @param name The name of the collection
     * @param httpIntf The HTTP interface to use to access the Collection
     */
    public Collection(String name, HttpInterface httpIntf) {
        super("/" + percentEncode(name), httpIntf);
        _name = name;
    }

    /**
     * Returns a DocumentSearcher instance to search for specific documents in
     * this collection.
     */
    public DocumentSearcher documents() {
        return new DocumentSearcher(this, _httpIntf);
    }

    /// The name of the collection (un-escaped)
    private final String _name;
}
