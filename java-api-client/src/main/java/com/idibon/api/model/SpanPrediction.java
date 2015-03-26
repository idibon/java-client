/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;

/**
 * Prediction results for {@link com.idibon.api.model.Task.Scope.span}-scope
 * tasks.
 *
 * A prediction result may include predictions for any number of
 * {@link com.idibon.api.model.SpanPrediction.Span} regions, and multiple
 * spans may overlap the same text region, possibly with different predicted
 * labels.
 */
public class SpanPrediction implements Prediction<SpanPrediction> {

    /**
     * A specific region of text within a
     * {@link com.idibon.api.model.DocumentContent} that is predicted as
     * being one of the entities in a span-scope prediction task.
     */
    public class Span {
        /**
         * The predicted Label for this Span.
         */
        public final Label label;
        /**
         * The confidence that this prediction is correct.
         */
        public final double confidence;
        /**
         * The offset (UTF-16 character index) in the document content where
         * the Span begins, inclusive.
         */
        public final int offset;
        /**
         * The length (in UTF-16 characters) of the Span in the document
         * content.
         */
        public final int length;
        /**
         * The actual text of the Span.
         */
        public final String text;

        Span(Label label, double conf, int offs, int length, String text) {
            this.label = label;
            this.confidence = conf;
            this.offset = offs;
            this.length = length;
            this.text = text;
        }
    }

    /**
     * Returns the {@link com.idibon.api.model.DocumentContent} object that
     * was used to generate this prediction.
     *
     * @return The DocumentContent that generated this prediction.
     */
    public DocumentContent getRequested() {
        return _requested;
    }

    /**
     * Returns the {@link com.idibon.api.model.DocumentContent} object that
     * was used to generate this prediction, cast to a user-defined type.
     *
     * @param clazz Specific implementation of DocumentContent that should
     *              be returned.
     * @return The DocumentContent that generated this prediction, cast to
     *         be of type clazz.
     */
    public <T extends DocumentContent> T getRequestedAs(Class<T> clazz) {
        try {
            return clazz.cast(_requested);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Invalid class", ex);
        }
    }

    /**
     * Returns the {@link com.idibon.api.model.Task} that made this
     * prediction.
     *
     * @return {@link com.idibon.api.model.Task}
     */
    public Task getTask() {
        return _task;
    }

    /**
     * Returns the raw JSON prediction output.
     *
     * @return JsonArray of span prediction JsonObjects
     */
    public JsonArray getJson() {
        return _rawPredictions;
    }

    /**
     * Returns all predicted spans, ordered by offset.
     *
     * @return List of {@link com.idibon.api.model.SpanPrediction.Span} results
     */
    public List<Span> getSpans() {
        List<Span> result = new ArrayList<>(_rawPredictions.size());
        for (JsonObject pred : _rawPredictions.getValuesAs(JsonObject.class)) {
            result.add(new Span(
              _task.label(pred.getString("class")),
              pred.getJsonNumber("confidence").doubleValue(),
              pred.getJsonNumber("length").intValue(),
              pred.getJsonNumber("offset").intValue(),
              pred.getString("text")));
        }
        return Collections.unmodifiableList(result);
    }

    SpanPrediction(JsonArray v, DocumentContent requested, Task task) {
        _requested = requested;
        _rawPredictions = v;
        _task = task;
    }

    private final JsonArray _rawPredictions;
    private final DocumentContent _requested;
    private final Task _task;
}
