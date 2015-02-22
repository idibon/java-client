/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;

import com.idibon.api.http.*;
import javax.json.*;

import javax.json.JsonValue.ValueType;

/**
 * Base class for all data model objects that are loadable via direct
 * HTTP GET operations from the Idibon API.
 */
public class IdibonHash {

    /**
     * Returns the JSON hash for this object.
     */
    public JsonObject getJson() throws IOException {
        Future<JsonValue> async = null;
        synchronized(this) {
            if (_jsonFuture == null)
                _jsonFuture = _httpIntf.httpGet(_endpoint, null);
            async = _jsonFuture;
        }

        JsonObject result = null;

        try {
            JsonValue element = async.get();
            if (element.getValueType() != ValueType.OBJECT)
                throw new IOException("Invalid return object");
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

    /**
     * Returns a percent-encoded string suitable for use in URL paths.
     *
     * @param str String to encode
     * @returns Encoded string
     */
    public static String percentEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new Error("Impossible", ex);
        }
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
    @SuppressWarnings("unchecked")
    protected <T extends IdibonHash> T preload(final JsonObject data) {
        /* Create a trivial Future instance for data that doesn't need
         * to be asynchronously loaded. Done outside the mutex to avoid
         * unnecessary object allocations inside a lock */
        Future<JsonValue> preloaded = new Future<JsonValue>() {
            public boolean cancel(boolean ignore) { return false; }
            public JsonValue get() { return data; }
            public JsonValue get(long t, TimeUnit u) { return data; }
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
    private Future<JsonValue> _jsonFuture;
}
