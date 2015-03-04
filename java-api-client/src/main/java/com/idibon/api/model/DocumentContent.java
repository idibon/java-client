/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import javax.json.JsonObject;
import java.util.List;

/**
 * Common methods for interacting with various types of document content
 * that is stored by the API.
 */
public interface DocumentContent {

    /**
     * Returns the content
     */
    public String getContent() throws IOException;

    /**
     * Returns the metadata
     */
    public JsonObject getMetadata() throws IOException;

    /**
     * Interface implemented by documents that have a known name. Documents
     * returned by the API will always have a name, but this is optional
     * for user-created documents that are uploaded to the API.
     */
    public interface Named extends DocumentContent {
        /**
         * Returns the document name.
         */
        public String getName();
    }

    /**
     * Interface implemented by documents that have annotation data.
     *
     * Documents returned by the API will always implement this interface.
     */
    public interface Annotated extends DocumentContent {
        /**
         * Returns the annotations on this document.
         *
         * The list may be empty if no annotations exist, or if no annotations
         * match a search term in a
         * {@link com.idibon.api.model.DocumentAnnotationQuery}.
         */
        public List<? extends Annotation> getAnnotations() throws IOException;
    }
}
