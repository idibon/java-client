/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import javax.json.*;

/**
 * Document classification prediction results.
 */
public class DocumentPrediction implements Prediction<DocumentPrediction> {

    public Predictable getPredictable() {
        return _predictable;
    }

    public <T extends Predictable> T getPredictableAs(Class<T> clazz) {
        try {
            return clazz.cast(_predictable);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Invalid class", ex);
        }
    }

    public JsonArray getJson() {
        return _rawPredictions;
    }

    public void init(JsonArray v, Predictable predictable) {
        _predictable = predictable;
        _rawPredictions = v;
    }

    private JsonArray _rawPredictions;
    private Predictable _predictable;
}
