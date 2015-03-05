/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import javax.json.JsonArray;

/**
 * Common super-interface for any type (e.g., document vs span scope)
 * of prediction API result.
 */
public interface Prediction<T extends Prediction> {
    /**
     * Returns the item that was submitted to the prediction API.
     */
    DocumentContent getRequested();

    /**
     * Returns a typesafe cast of the item submitted to the prediction API.
     */
    <T extends DocumentContent> T getRequestedAs(Class<T> clazz);

    /**
     * Returns the raw JSON predictions returned by the API.
     */
    JsonArray getJson();

    /**
     * Initializes the instance using the provided raw results
     * returned by the API.
     */
    void init(JsonArray v,  DocumentContent predictable);
}
