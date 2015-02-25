/*
 * Copyright (c) 2015, Idibon, Inc.
 */

package com.idibon.api.http;

import java.io.*;
import java.util.concurrent.Future;
import javax.json.*;

/**
 * Generic interface abstracting the HTTP methods that are used by the
 * Idibon API client.
 */
public interface HttpInterface {

    /**
     * Executes an HTTP GET method with no parameters to the specified
     * endpoint.
     *
     * @param endpoint URI-encoded endpoint for the request
     *
     * @return Decoded JSON response from the server
     */
    public Future<JsonValue> httpGet(String endpoint)
        throws IOException;

    /**
     * Executes an HTTP GET method and transmits the serialized body to the
     * specified endpoint.
     *
     * @param endpoint URI-encoded endpoint for the request
     * @param body HTTP request body
     *
     * @return Decoded JSON response from the server
     */
    public Future<JsonValue> httpGet(String endpoint, JsonObject body)
        throws IOException;

    /**
     * Executes an HTTP PUT method and transmits the serialized body to the
     * specified endpoint.
     *
     * @param endpoint URI-encoded endpoint for the request
     * @param body HTTP request body
     *
     * @return Decoded JSON response from the server
     */
    public Future<JsonValue> httpPut(String endpoint, JsonObject body)
        throws IOException;

    /**
     * Executes an HTTP POST method and transmits the serialized body to the
     * specified endpoint.
     *
     * @param endpoint URI-encoded endpoint for the request
     * @param body HTTP request body
     *
     * @return Decoded JSON response from the server
     */
    public Future<JsonValue> httpPost(String endpoint, JsonObject body)
        throws IOException;

    /**
     * Executes an HTTP DELETE method and transmits the serialized body to the
     * specified endpoint.
     *
     * @param endpoint URI-encoded endpoint for the request
     * @param body HTTP request body
     *
     * @return Decoded JSON response from the server
     */
    public Future<JsonValue> httpDelete(String endpoint, JsonObject body)
        throws IOException;

    /**
     * Waits for pending operations to complete and shuts down any execution
     * threads that were created.
     *
     * @param quiesceTime The maximum time to wait, in milliseconds.
     */
    public void shutdown(long quiesceTime);
}
