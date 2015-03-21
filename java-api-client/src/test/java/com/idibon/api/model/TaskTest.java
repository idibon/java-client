/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import org.junit.*;
import javax.json.*;
import java.util.*;

import java.io.StringReader;
import com.idibon.api.model.Collection;

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
        assertThat(mockTask.getSubtasks().keySet(), is(empty()));
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

    @SuppressWarnings("unchecked")
    @Test public void testLoadOntology() throws Exception {
        String json = "{\"task\":{\"scope\":\"document\",\"labels\":[{\"name\":\"label\"}]," +
            "\"uuid\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"task\"," +
            "\"config\":{\"sub_tasks\":{\"label\":[\"sub1\",\"sub2\"]}}}}";
        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        Map<Label, Set<? extends Task>> ontology = mockTask.getSubtasks();
        Label l = mockTask.label("label");
        assertThat(ontology.keySet(), is((Set)new HashSet(Arrays.asList(l))));
        Set<? extends Task> triggered = ontology.get(l);
        assertThat(triggered, is(
            (Set)new HashSet(Arrays.asList(mockCollection.task("sub1"),
                                           mockCollection.task("sub2")))));
    }
}
