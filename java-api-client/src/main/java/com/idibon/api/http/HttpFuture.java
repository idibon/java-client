/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http;

import java.io.IOException;
import java.util.concurrent.*;
import javax.json.JsonValue;

import com.idibon.api.util.Either;

/**
 * Wraps the result of a {@link java.util.concurrent.Future}, either
 * exceptional or succsesful in an Either monad, suppressing the thrown
 * exception.
 */
public final class HttpFuture<Result extends JsonValue>
    implements Future<Either<IOException, Result>> {

    public boolean isDone() {
        return _base.isDone();
    }

    public boolean isCancelled() {
        return _base.isCancelled();
    }

    public boolean cancel(boolean mayInterrupt) {
        return _base.cancel(mayInterrupt);
    }

    /**
     * Casts a successful result to a subclass of the standard Result type.
     *
     * @param clazz The expected type of a successful return value.
     * @return {@link com.idibon.api.util.Either} with a left IOException and
     *         a right of type clazz
     */
    public <T extends Result> Either<IOException, T> getAs(Class<T> clazz) {
        Either<IOException, Result> raw = get();
        if (raw.isLeft()) return Either.left(raw.left);
        try {
            return Either.right(clazz.cast(raw.right));
        } catch (ClassCastException ex) {
            return Either.left(new IOException("Invalid server response"));
        }
    }

    public Either<IOException, Result> get() {
        return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public Either<IOException, Result> get(long timeout, TimeUnit unit) {
        try {
            return Either.right(_base.get(timeout, unit));
        } catch (ExecutionException ex) {
            Throwable actual = ex.getCause();
            if (actual instanceof IOException)
                return Either.left((IOException)actual);
            return Either.left(new IOException("Async exception", actual));
        } catch (TimeoutException ex) {
            IOException err = new IOException("Operation failed to complete");
            err.setStackTrace(ex.getStackTrace());
            return Either.left(err);
        } catch (CancellationException ex) {
            IOException err = new IOException("Operation cancelled");
            err.setStackTrace(ex.getStackTrace());
            return Either.left(err);
        } catch (InterruptedException ex) {
            IOException err = new IOException("Operation interrupted");
            err.setStackTrace(ex.getStackTrace());
            return Either.left(err);
        }
    }

    /**
     * Converts a {@link java.util.concurrent.Future} that returns a JsonValue
     * or throws an Exception into an HttpFuture that returns an Either.
     */
    public static <R extends JsonValue> HttpFuture<R> wrap(Future<R> base) {
        return new HttpFuture<>(base);
    }

    protected HttpFuture(Future<Result> base) {
        _base = base;
    }

    private final Future<Result> _base;
}
