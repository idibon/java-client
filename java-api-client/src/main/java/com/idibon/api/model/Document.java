/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import com.idibon.api.http.*;
import javax.json.JsonObject;

/**
 * A Document is the basic unit of content in a Collection
 */
public class Document extends IdibonHash {

    /**
     * Returns the raw JSON data for this Document
     */
    @Override public JsonObject getJson() throws IOException {
        return super.getJson().getJsonObject("document");
    }

    public Document(String name, Collection parent, HttpInterface httpIntf) {
        super(parent.getEndpoint() + "/" + percentEncode(name), httpIntf);
        _name = name;
        _parent = parent;
    }

    private final Collection _parent;
    private final String _name;
}
