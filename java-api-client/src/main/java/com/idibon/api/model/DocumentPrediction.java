/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;

/**
 * Document classification prediction results.
 */
public class DocumentPrediction implements Prediction<DocumentPrediction> {

    public DocumentContent getRequested() {
        return _requested;
    }

    public Task getTask() {
        return _task;
    }

    public <T extends DocumentContent> T getRequestedAs(Class<T> clazz) {
        try {
            return clazz.cast(_requested);
        } catch (ClassCastException ex) {
            throw new RuntimeException("Invalid class", ex);
        }
    }

    public JsonArray getJson() {
        return _rawPredictions;
    }

    /**
     * Returns the predicted confidences for the requested DocumentContent
     * against all Labels in the Task.
     *
     * @return A {@link java.util.Map} of {@link com.idibon.api.model.Label}
     *    to the prediction confidence for that Label.
     */
    public Map<Label, Double> getPredictedConfidences() throws IOException {
        JsonObject classes = _rawPredictions.getJsonObject(0)
            .getJsonObject("classes");

        if (classes == null)
            throw new IOException("API returned no data.");

        Map<Label, Double> confMap = new HashMap<>();
        for (Map.Entry<String, JsonValue> entry : classes.entrySet()) {
            Label label = _task.label(entry.getKey());
            if (entry.getValue() instanceof JsonNumber) {
                JsonNumber number = (JsonNumber)entry.getValue();
                confMap.put(label, Double.valueOf(number.doubleValue()));
            } else {
                confMap.put(label, Double.valueOf(Double.NaN));
            }
        }
        return Collections.unmodifiableMap(confMap);
    }

    /**
     * Returns the document features (word, n-grams, etc.) from the document
     * that were most significant for each label.
     *
     * Will return <tt>null</tt> if significant features were not requested
     * by the {@link com.idibon.api.model.PredictionIterable} that generated
     * this prediction.
     *
     * @return A {@link java.util.Map} of {@link com.idibon.api.model.Label}
     *    to a {@link java.util.List} of the features that influenced the
     *    prediction above the requested significant feature threshold.
     */
    public Map<Label, List<String>> getSignificantFeatures() {
        JsonObject features = _rawPredictions.getJsonObject(0)
            .getJsonObject("features");

        if (features == null) return null;
        Map<Label, List<String>> featMap = new HashMap<>();
        for (Map.Entry<String, JsonValue> entry : features.entrySet()) {
            Label label = _task.label(entry.getKey());
            List<String> labelFeats;

            if (entry.getValue() instanceof JsonObject) {
                JsonObject featuresAndWeights = (JsonObject)entry.getValue();
                labelFeats = new ArrayList<>(featuresAndWeights.size());
                for (String feature : featuresAndWeights.keySet())
                    labelFeats.add(feature);
            } else {
                labelFeats = Collections.emptyList();
            }

            featMap.put(label, Collections.unmodifiableList(labelFeats));
        }

        return Collections.unmodifiableMap(featMap);
    }

    DocumentPrediction(JsonArray v, DocumentContent requested, Task task) {
        _requested = requested;
        _rawPredictions = v;
        _task = task;
    }

    private final JsonArray _rawPredictions;
    private final DocumentContent _requested;
    private final Task _task;
}
