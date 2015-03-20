/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.idibon.api.http.*;
import javax.json.*;

import javax.json.JsonValue.ValueType;

/**
 * Base class for all data model objects that are loadable via direct
 * HTTP GET operations from the Idibon API.
 */
public abstract class IdibonHash {

    /**
     * Returns the raw JSON body representing this object.
     */
    public abstract JsonObject getJson() throws IOException;

    /**
     * Returns the configured endpoint for this object.
     */
    public String getEndpoint() {
        return _endpoint;
    }

    /**
     * Returns the HTTP interface used to interact with this object.
     */
    public HttpInterface getInterface() {
        return _httpIntf;
    }

    /**
     * Returns true if all of the data for the object has been loaded
     */
    public boolean isLoaded() {
        return _jsonFuture != null && _jsonFuture.isDone();
    }

    /**
     * Returns a percent-encoded string suitable for use in URL paths.
     *
     * @param str String to encode
     * @return Encoded string
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
     * Invalidates all cached data in this object, causing data to be reloaded
     * from the server.
     */
    @SuppressWarnings("unchecked")
    public <T extends IdibonHash> T invalidate() {
        synchronized(this) {
            _jsonFuture = null;
        }
        return (T)this;
    }

    /**
     * Returns the JSON hash for this object, with an optional body query
     * parameter.
     *
     * @param body Body to include with HTTP query. May be null.
     */
    protected JsonObject getJson(JsonObject body) throws IOException {
        HttpFuture<JsonValue> async = null;

        synchronized(this) {
            if (_jsonFuture == null)
                _jsonFuture = _httpIntf.httpGet(_endpoint, body);
            async = _jsonFuture;
        }

        try {
            if (async.get().isLeft())
                throw async.get().left;

            JsonValue element = async.get().right;
            if (element.getValueType() != ValueType.OBJECT)
                throw new IOException("Invalid return object type from API");

            return (JsonObject)element;
        } catch (IOException ex) {
            /* since the load failed, clear out the cached future so that it
             * will be tried again on the next request. */
            synchronized(this) {
                if (async == _jsonFuture)
                    _jsonFuture = null;
            }
            throw ex;
        }
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
        synchronized(this) { _jsonFuture = HttpFuture.wrap(preloaded); }
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
    private HttpFuture<JsonValue> _jsonFuture;
}
