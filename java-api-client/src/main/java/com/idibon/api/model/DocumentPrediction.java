/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import javax.json.*;

/**
 * Document classification prediction results.
 */
public class DocumentPrediction implements Prediction<DocumentPrediction> {

    public DocumentContent getRequested() {
        return _requested;
    }

    public <T extends DocumentContent> T getRequestedAs(Class<T> clazz) {
        try {
            return clazz.cast(_requested);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Invalid class", ex);
        }
    }

    public JsonArray getJson() {
        return _rawPredictions;
    }

    public void init(JsonArray v, DocumentContent requested) {
        _requested = requested;
        _rawPredictions = v;
    }

    private JsonArray _rawPredictions;
    private DocumentContent _requested;
}
