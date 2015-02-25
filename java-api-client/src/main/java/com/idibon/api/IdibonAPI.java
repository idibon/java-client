/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

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
