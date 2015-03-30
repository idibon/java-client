/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.Map;
import java.util.List;

import org.junit.*;
import javax.json.*;
import java.io.StringReader;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class DocumentPredictionTest {

    @Test public void testNullFeatures() throws Exception {
        String json = "[{\"class\":\"A\",\"classes\":{\"A\":0.6,\"B\":0.5},\"confidence\":0.6}]";
        JsonArray predictions = Json.createReader(new StringReader(json)).readArray();
        Task mockTask = Collection.instance(null, "C").task("task");
        DocumentPrediction p = new DocumentPrediction(predictions, null, mockTask);
        assertThat(p.getSignificantFeatures(), is(nullValue()));
        Map<Label, Double> confidence = p.getPredictedConfidences();
        assertThat(confidence.get(mockTask.label("A")).doubleValue(), is(closeTo(0.6, 0.001)));
        assertThat(confidence.get(mockTask.label("B")).doubleValue(), is(closeTo(0.5, 0.001)));
    }

    @Test public void testWithFeatures() throws Exception {
        String json = "[{\"class\":\"A\",\"classes\":{\"A\":0.6,\"B\":0.5},\"confidence\":0.6," +
            "\"features\":{\"A\":{\"WAT\":0.7}}}]";
        JsonArray predictions = Json.createReader(new StringReader(json)).readArray();
        Task mockTask = Collection.instance(null, "C").task("task");
        DocumentPrediction p = new DocumentPrediction(predictions, null, mockTask);
        Map<Label, List<String>> features = p.getSignificantFeatures();
        assertThat(features, is(not(nullValue())));
        assertThat(features.get(mockTask.label("B")), is(nullValue()));
        assertThat(features.get(mockTask.label("A")), hasItems("WAT"));
    }
}
