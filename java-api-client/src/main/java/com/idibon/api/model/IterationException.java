/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

/**
 * Exception class thrown when errors occur during iteration over model
 * search results.
 */
public class IterationException extends RuntimeException {
    public IterationException(String msg) {
        super(msg);
    }

    public IterationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
