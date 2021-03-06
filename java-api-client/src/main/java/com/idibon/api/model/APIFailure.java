/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

/**
 * The APIFailure represents a failed API request, such as a document
 * or annotation upload, containing the item(s) in the failed request
 * and the error itself.
 */
public class APIFailure<T> {
    /**
     * The exception generated by the request.
     */
    public final IOException exception;
    /**
     * The original item(s) in the request.
     */
    public final T request;

    /**
     * Returns a new APIFailure instance.
     */
    static <U> APIFailure<U> failure(IOException exception, U request) {
        return new APIFailure<>(exception, request);
    }

    protected APIFailure(IOException exception, T request) {
        this.exception = exception;
        this.request = request;
    }
}
