/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.Iterator;

import com.idibon.api.util.Either;

/**
 * Generates predictions for one or more predictable items.
 */
public interface PredictionIterable<T extends Prediction> 
	extends Iterable<Either<APIFailure<DocumentContent>, T>> {

    /**
     * Default feature threshold. Returns moderate-strongly predictive features
     */
    public static final double DEFAULT_FEATURE_THRESHOLD = 0.7;

    public Iterator<Either<APIFailure<DocumentContent>, T>> iterator();

    /**
     * Returns the key words and phrases from the document content that
     * affected the prediction.
     *
     * This is the same as calling
     * {@link com.idibon.api.model.PredictionIterable#withSignificantFeatures(double)}
     * with a value of DEFAULT_FEATURE_THRESHOLD.
     *
     * @return This
     */
    public PredictionIterable<T> withSignificantFeatures();

    /**
     * Returns words and phrases from the document content that affected
     * the prediction above the provided threshold.
     *
     * @param threshold Defines the cutoff threshold to include features.
     *        Should be 0.0 - 1.0.
     * @return This
     */
    public PredictionIterable<T> withSignificantFeatures(double threshold);
}
