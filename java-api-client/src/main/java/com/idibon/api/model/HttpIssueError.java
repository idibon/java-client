/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import javax.json.JsonValue;

/**
 * Wraps a Future interface around an exception that occurs while constructing
 * an HTTP operation, so that batch-issued operations can deliver the error
 * using the Either monad.
 */
final class HttpIssueError implements Future<JsonValue> {

    public boolean isDone() {
        return true;
    }

    public boolean cancel(boolean mayInterrupt) {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public JsonValue get() throws ExecutionException {
        return get(0, TimeUnit.MILLISECONDS);
    }

    public JsonValue get(long t, TimeUnit u) throws ExecutionException {
        throw new ExecutionException("Failed", _error);
    }

    static HttpIssueError wrap(Throwable error) {
        return new HttpIssueError(error);
    }

    HttpIssueError(Throwable error) {
        _error = error;
    }

    private final Throwable _error;
}
