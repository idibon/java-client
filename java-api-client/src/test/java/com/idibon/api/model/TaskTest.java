/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import org.junit.*;
import javax.json.*;
import java.util.List;

import java.io.StringReader;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class TaskTest {

    @Test public void testNullConfig() throws Exception {
        // shouldn't crash reading subtasks / tuning rules if config is missing
        String json = "{\"task\":{\"scope\":\"document\",\"labels\":[]," +
            "\"uuid\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"task\"}}";

        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        assertThat(mockTask.getRules().keySet(), is(empty()));
    }

    @Test public void testLoadRules() throws Exception {
        String json = "{\"task\":{\"scope\":\"document\",\"labels\":[]," +
            "\"uuid\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"task\"," +
            "\"config\":{\"tuning\":{\"Label\":{\"substring\":0.8,\"/regex/\":0.2}}}}}";
        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        assertThat(mockTask.getRules(), hasKey(mockTask.label("Label")));
        List<? extends TuningRules.Rule> rules = mockTask.label("Label").getRules();
        assertThat(rules, hasSize(2));
        assertThat(rules.get(0), is(instanceOf(TuningRules.Rule.Substring.class)));
        assertThat(rules.get(1), is(instanceOf(TuningRules.Rule.Regex.class)));
    }
}
