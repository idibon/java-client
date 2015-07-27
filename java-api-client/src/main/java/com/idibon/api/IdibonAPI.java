/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.io.IOException;
import java.lang.reflect.Method;

import java.util.Arrays;

import javax.json.*;

import com.idibon.api.model.*;
import com.idibon.api.http.*;
import com.idibon.api.util.Either;
import com.idibon.api.util.Memoize;

public class IdibonAPI {

    /**
     * Exception thrown when client attempts to create a collection that
     * already exists on the server.
     */
    public static class CollectionAlreadyExistsException extends IOException {
        public CollectionAlreadyExistsException(String name) {
            super("Collection already exists with name: " + name);
        }
    }

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
        return _collections.memoize(instantiateCollection(name));
    }

    /**
     * Creates a new collection in the API.
     *
     * Verifies that no collection with the same name already exists, then
     * creates the new collection.
     *
     * @param name Name for the new collection; must start with a letter a-z
     *        or A-Z, and all characters must be valid characters in DNS names.
     * @param description Description for the new collection.
     */
    public Collection createCollection(String name, String description)
            throws IOException {
        mustHaveInterface();
        Collection existingOrNew = collection(name);
        try {
            existingOrNew.invalidate().get(Collection.Keys.uuid);
            /* if this method completes successfully, the collection already
             * exists; throw an exception in this case. */
            throw new CollectionAlreadyExistsException(name);
        } catch (HttpException.NotFound _) {
            // ignore this exception, this is the desired behavior
        }

        validateCollectionName(name);
        JsonObject json = Json.createObjectBuilder()
            .add("collection", Json.createObjectBuilder()
                 .add(Collection.Keys.description.name(), description)
                 .build()).build();

        Either<IOException, JsonObject> result = _httpIntf
            .httpPut(existingOrNew.getEndpoint(), json)
            .getAs(JsonObject.class);

        if (result.isLeft()) throw result.left;

        try {
            Method preload = IdibonHash.class.getDeclaredMethod(
                "preload", JsonObject.class);
            preload.setAccessible(true);
            existingOrNew.invalidate();
            return (Collection)preload.invoke(existingOrNew, result.right);
        } catch (Exception ex) {
            throw new Error("Method invocation problem", ex);
        }
    }

    /**
     * Wait for one or more asynchronous operations to complete.
     *
     * Same as calling #waitFor(Arrays.asList(futures)).
     *
     * @param futures List of operations to synchronize on completion.
     */
    public void waitFor(HttpFuture<?>... futures) throws IOException {
        waitFor(Arrays.asList(futures));
    }

    /**
     * Wait for one or more asynchronous operations to complete.
     *
     * If an exception occurs on one of the pending operations, the method
     * waits for other operations to complete then throws the exception that
     * occurred.
     *
     * @param futures List of operations to wait for completion.
     */
    public void waitFor(Iterable<HttpFuture<?>> futures) throws IOException {
        IOException err = null;
        for (HttpFuture<?> f : futures) {
            Either<IOException, ?> result = f.get();
            if (result.isLeft() && err == null) err = result.left;
        }

        if (err != null) throw err;
    }

    /**
     * Uses reflection and security over-rides to instantiate a new Collection
     * from outside the package.
     */
    private Collection instantiateCollection(String name) {
        try {
            Method collInstance = Collection.class.getDeclaredMethod(
                "instance", HttpInterface.class, String.class);
            collInstance.setAccessible(true);
            return (Collection)collInstance.invoke(null, _httpIntf, name);
        } catch (Exception _) {
            throw new Error(""); // can't happen
        }
    }

    /**
     * Verifies that an interface has been assigned before executing any
     * operations that might communicate over HTTP.
     */
    private void mustHaveInterface() {
        if (_httpIntf == null)
            throw new IllegalStateException("Interface is not configured");
    }

    /**
     * Verifies that new collection names conform to the API restrictions
     * on the supported character set for collection names.
     */
    private static void validateCollectionName(String name) {
        if (name.length() == 0)
            throw new IllegalArgumentException("empty names not allowed");

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            boolean isLatinLetter = (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z');
            boolean isLatinDigit = (c >= '0' && c <= '9');
            boolean isDnsPunctuation = (c == '_' || c == '-');

            if (i == 0 && !isLatinLetter)
                throw new IllegalArgumentException("first char not letter");
            else if (!(isLatinLetter || isLatinDigit || isDnsPunctuation))
                throw new IllegalArgumentException("invalid char: " + c);
        }
    }

    // HTTP Interface used for all server communication
    private HttpInterface _httpIntf;

    // Memoization for Collection instances
    private final Memoize<Collection> _collections =
        Memoize.cacheReferences(Collection.class);
}
