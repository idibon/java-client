/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import org.junit.*;
import javax.json.*;

import java.io.StringReader;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class LabelTest {

    private void testConfidenceThreshold(String json,
            String labelName, double expected) throws Exception {

        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        Label mockLabel = mockTask.label(labelName);
        assertThat(mockLabel.getConfidenceThreshold(), is(equalTo(expected)));
    }

    @Test public void testCustomConfidenceThreshold() throws Exception {
        String json =
          "{\"task\":{" +
              "\"scope\":\"document\"," +
              "\"config\":{" +
               "\"confidence_thresholds\":{\"labels\":{" +
                 "\"Label\":{\"suggested\":0.75}" +
               "}}" +
              "},"+
              "\"labels\":[" +
                "{\"name\":\"Label\",\"description\":\"\"," +
                 "\"uuid\":\"00000000-0000-0000-0000-000000000001\"}]," +
              "\"uuid\":\"00000000-0000-0000-0000-000000000000\"," +
              "\"name\":\"task\"}}";
        testConfidenceThreshold(json, "Label", 0.75);
    }

    @Test public void testDefaultConfidenceThreshold() throws Exception {
        String json =
          "{\"task\":{" +
              "\"scope\":\"document\"," +
              "\"labels\":[" +
                "{\"name\":\"Label\",\"description\":\"\"," +
                 "\"uuid\":\"00000000-0000-0000-0000-000000000001\"}]," +
              "\"uuid\":\"00000000-0000-0000-0000-000000000000\"," +
              "\"name\":\"task\"}}";
        testConfidenceThreshold(json, "Label", Label.DEFAULT_CONFIDENCE_THRESHOLD);
    }

    @Test public void testDescription() throws Exception {
        String json =
          "{\"task\":{" +
              "\"scope\":\"document\"," +
              "\"labels\":[" +
                "{\"name\":\"Boo!\",\"description\":\"A Ghost!\"," +
                 "\"uuid\":\"00000000-0000-0000-0000-000000000001\"}]," +
              "\"uuid\":\"00000000-0000-0000-0000-000000000000\"," +
              "\"name\":\"task\"}}";

        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        Label mockLabel = mockTask.label("Boo!");
        assertThat(mockLabel.getDescription(), is(equalTo("A Ghost!")));
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testInvalidLabel() throws Exception {
        String json =
          "{\"task\":{" +
              "\"scope\":\"document\"," +
              "\"labels\":[" +
                "{\"name\":\"Label\",\"description\":\"\"," +
                 "\"uuid\":\"00000000-0000-0000-0000-000000000001\"}]," +
              "\"uuid\":\"00000000-0000-0000-0000-000000000000\"," +
              "\"name\":\"task\"}}";

        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        Label mockLabel = mockTask.label("Boo!");
        mockLabel.getDescription();
    }
}
