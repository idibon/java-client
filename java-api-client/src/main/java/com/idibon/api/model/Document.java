/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.net.URLEncoder;
import java.io.IOException;

import com.idibon.api.http.*;
import com.google.gson.*;

/**
 * A Document is the basic unit of content in a Collection
 */
public class Document extends IdibonHash {

    /**
     * Returns the raw JSON data for this Document
     */
    @Override public JsonObject getJson() throws IOException {
        return super.getJson().get("document").getAsJsonObject();
    }

    public Document(String name, Collection parent, HttpInterface httpIntf) {
        super(parent.getEndpoint() + "/" +
              URLEncoder.encode(name).replace("+", "%20"), httpIntf);
        _name = name;
        _parent = parent;
    }

    private final Collection _parent;
    private final String _name;
}
