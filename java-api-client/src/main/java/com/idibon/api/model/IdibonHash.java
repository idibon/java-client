/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import com.idibon.api.http.*;
import com.google.gson.*;

/**
 * Base class for all data model objects that are loadable via direct
 * HTTP GET operations from the Idibon API.
 */
public class IdibonHash {

    /**
     * Returns the JSON hash for this object.
     */
    public JsonObject getJson() throws IOException {
        Future<JsonElement> async = null;
        synchronized(this) {
            if (_jsonFuture == null)
                _jsonFuture = _httpIntf.httpGet(_endpoint, null);
            async = _jsonFuture;
        }

        JsonObject result = null;

        try {
            JsonElement element = async.get();
            if (!element.isJsonObject())
                throw new IOException("Invalid return object");
            else
                result = (JsonObject)element;
        } catch (InterruptedException ex) {
            throw new IOException("Interrupted", ex);
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException)
                throw (IOException)(ex.getCause());
            else
                throw new IOException("Wrapped IOException", ex);
        } finally {
            /* if the load fails for any reason, clear out the cached Future
             * so that the load will be tried again. */
            if (result == null) {
                synchronized(this) {
                    if (_jsonFuture == async)
                        _jsonFuture = null;
                }
            }
        }

        return result;
    }

    /**
     * Returns the configured endpoint for this object.
     */
    public String getEndpoint() {
        return _endpoint;
    }

    @Override public int hashCode() {
        return _endpoint.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj.getClass() != getClass()) return false;

        IdibonHash h = (IdibonHash)obj;
        return _endpoint.equals(h._endpoint) && _httpIntf == h._httpIntf;
    }

    /**
     * Uses already-available data to configure the lazy-loaded JSON object.
     */
    protected <T extends IdibonHash> T preload(final JsonObject data) {
        /* Create a trivial Future instance for data that doesn't need
         * to be asynchronously loaded. Done outside the mutex to avoid
         * unnecessary object allocations inside a lock */
        Future<JsonElement> preloaded = new Future<JsonElement>() {
            public boolean cancel(boolean ignore) { return false; }
            public JsonElement get() { return data; }
            public JsonElement get(long t, TimeUnit u) { return data; }
            public boolean isCancelled() { return false; }
            public boolean isDone() { return true; }
        };
        synchronized(this) { _jsonFuture = preloaded; }
        return (T)this;
    }

    protected IdibonHash(String endpoint, HttpInterface httpIntf) {
        _httpIntf = httpIntf;
        _endpoint = endpoint;
    }

    /// The HTTP interface to use for (re)loading the model from the API
    protected final HttpInterface _httpIntf;

    /// The API endpoint where the model can be (re)loaded
    protected final String _endpoint;

    /// The JSON hash of data for this object. Potentially-lazy-loaded.
    private Future<JsonElement> _jsonFuture;
}
