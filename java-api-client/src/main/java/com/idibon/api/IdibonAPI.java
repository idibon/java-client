/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.io.IOException;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import javax.json.JsonValue;

import com.idibon.api.model.*;
import com.idibon.api.http.*;

public class IdibonAPI {

    /**
     * Configure the HTTP interface that the client should use.
     *
     * Must be called exactly once.
     *
     * @param httpIntf {@link com.idibon.api.http.HttpInterface} implementation
     *        to use for this client instance.
     */
    public IdibonAPI using(HttpInterface httpIntf) {
        if (_httpIntf != null)
            throw new IllegalStateException("Interface may not be re-configured");
        _httpIntf = httpIntf;
        return this;
    }

    /**
     * Waits for pending operations to complete and shuts down any execution
     * threads that were created.
     *
     * @param quiesceTime The maximum time to wait, in milliseconds.
     */
    public void shutdown(long quiesceTime) {
        _httpIntf.shutdown(quiesceTime);
    }

    /**
     * Returns the cached Collection object with the provided name.
     */
    public Collection collection(String name) {
        mustHaveInterface();
        return Collection.instance(_httpIntf, name);
    }

    /**
     * Wait for one or more asynchronous operations to complete.
     *
     * Same as calling #waitFor(Arrays.asList(futures)).
     *
     * @param futures List of operations to synchronize on completion.
     */
    public void waitFor(Future<JsonValue>... futures) throws IOException {
        waitFor(Arrays.asList(futures));
    }

    public void waitFor(Iterable<Future<JsonValue>> futures) throws IOException {
        Throwable first = null;
        for (Future<JsonValue> f : futures) {
            try {
                f.get();
            } catch (ExecutionException ex) {
                if (first == null) first = ex.getCause();
            } catch (InterruptedException ex) {
                if (first == null) first = ex.getCause();
            }
        }

        if (first == null) return;
        if (first instanceof IOException) throw (IOException)first;
        throw new IOException("Exception waiting for results", first);
    }

    /**
     * Verifies that an interface has been assigned before executing any
     * operations that might communicate over HTTP.
     */
    private void mustHaveInterface() {
        if (_httpIntf == null)
            throw new IllegalStateException("Interface is not configured");
    }

    /// HTTP Interface used for all server communication
    private HttpInterface _httpIntf;
}
