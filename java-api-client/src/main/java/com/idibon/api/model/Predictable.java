/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import javax.json.JsonObject;

/**
 * Common interface for all data types that can be used as source data
 * for the predictive API, such as Document instances.
 */
public interface Predictable {

    /**
     * Returns a JsonObject to use as the body for a prediction request
     */
    public JsonObject createPredictionRequest();
}
