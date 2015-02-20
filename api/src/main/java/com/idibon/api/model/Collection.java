/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.net.URLEncoder;
import java.io.IOException;

import com.idibon.api.http.*;
import com.google.gson.*;

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
        return super.getJson().get("collection").getAsJsonObject();
    }

    /**
     * Constructor for Collections
     *
     * @param name The name of the collection
     * @param httpIntf The HTTP interface to use to access the Collection
     */
    public Collection(String name, HttpInterface httpIntf) {
        super("/" + URLEncoder.encode(name).replace("+", "%20"), httpIntf);
        _name = name;
    }

    /// The name of the collection (un-escaped)
    private final String _name;
}
