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
    
    @Test public void testTrivialRules() throws Exception {
        String json = "{\"task\":{\"uuid\":\"00000000-0000-0000-0000-000000000000\"," +
            "\"name\":\"ClassifyCats\",\"scope\":\"document\"," + 
            "\"labels\":[{\"name\":\"American Short Hair\"},{\"name\":\"Siamese\"}," +
            "{\"name\":\"Persian\"},{\"name\":\"Burmese\"},{\"name\":\"Siberian\"}," +
            "{\"name\":\"Balinese\"},{\"name\":\"Russian Blue\"},{\"name\":\"Maine Coon\"}]," +
            "\"features\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\"," +
            "\"name\":\"ClarabridgeRule\",\"parameters\":{\"label_rules\":" +
            "\"{\\\"American Short Hair\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]],\\\"Siamese\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," +
            "\\\"Persian\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," + 
            "\\\"Burmese\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]],\\\"Siberian\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," +
            "\\\"Balinese\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]],\\\"Russian Blue\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," +
            "\\\"Maine Coon\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]}\"},\"is_active\":true}]}}";
        
        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);

        /* Creating fake documents. This could really be anything, since it won't be used in 
         * an actual prediction. Since there are no rules, it is a trivial accept */
        Document mockDocument = Document.instance(mockCollection, "Cats are similar in anatomy to the " +
            "other felids, with strong, flexible bodies, quick reflexes, sharp retractable claws, " + 
            "and teeth adapted to killing small prey.");
        List<DocumentContent> docs = new ArrayList();
        for (int i = 0; i < 2; i++) {
            docs.add(mockDocument);
        }
        
        // Ensure that the top-level prediction is 1.0
        PredictionIterable<DocumentPrediction> prediction = mockTask.classifications(docs);
        JsonArray predictionJson = prediction.iterator().next().right.getJson();
        assert(predictionJson.getJsonObject(0).get("confidence").toString().equals("1.0"));
        
        // Ensure that all labels are predicted as 1.0
        JsonObject labels = predictionJson.getJsonObject(0).getJsonObject("classes");
        for (String label : labels.keySet()) {
            assert(labels.get(label).toString().equals("1.0"));
        }
        
        // Ensure that there are no features
        assert(predictionJson.getJsonObject(0).get("features") == null);
        
        // Specify that we want to see features, then ensure that an empty feature object is included
        prediction = prediction.withSignificantFeatures();
        predictionJson = prediction.iterator().next().right.getJson();
        assert(predictionJson.getJsonObject(0).get("features").toString().equals("{}"));
    }
    
    @Test public void testNontrivialRules() throws Exception {
        String json = "{\"task\":{\"uuid\":\"00000000-0000-0000-0000-000000000000\",\"name\":\"ClassifyCats\"," +
            "\"collection_id\":\"00000000-0000-0000-0000-000000000001\",\"description\":\"ClassifyCats\"," +
            "\"scope\":\"document\",\"trainable\":true,\"is_active\":true,\"created_at\":" +
            "\"2014-07-22T07:32:10Z\",\"updated_at\":\"2014-08-11T13:24:57Z\",\"trained_at\":" +
            "\"2014-07-28T21:00:01Z\",\"config\":{\"tuning\":{\"Persian\":{\"/(?i)white/\":0.1," +
            "\"/(?i)long.?hair/\":0.2},\"Siamese\":{\"/(?i)white/\":0.9,\"/(?i)short.?hair/\":0.9},\"Burmese\":{}," +
            "\"Siberian\":{},\"American Short Hair\":{\"/(?i)domestic.?short.?hair/\":0.9,\"/(?i)short.?hair/\":0.8}," +
            "\"Persian \":{}},\"lock\":{\"currently_training" +
            "\":false,\"training_started\":\"2014-07-28T20:59:59Z\",\"failure\":null},\"training_log" +
            "\":{\"aggregation_time\":null,\"extraction_time\":1.215,\"training_time\":1.469,\"overall_time" +
            "\":2.684},\"kit_type\":{\"type\":\"label\",\"multi_select\":true,\"show_none_button\":true," +
            "\"show_label_descriptions\":\"labels\",\"question_text\":\"What is the best label for this {{scope}}?" +
            "\",\"label_order\":[]}},\"touched_at\":\"2014-08-04T17:23:59Z\",\"labels\":[{\"name\":\"American Short Hair\"}," +
            "{\"name\":\"Siamese\"},{\"name\":\"Persian\"}],\"features\":[]}}";
        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        
        java.lang.reflect.Method method = Task.class.getDeclaredMethod("isTrivialAccept");
        method.setAccessible(true);

        assertThat(((boolean)method.invoke(mockTask)), is(false));
    }
    
    @Test public void testNontrivialRules2() throws Exception {
        String json = "{\"task\":{\"uuid\":\"00000000-0000-0000-0000-000000000000\"," +
                "\"name\":\"ClassifyCats\",\"scope\":\"document\"," + 
                "\"labels\":[{\"name\":\"American Short Hair\"},{\"name\":\"Siamese\"}," +
                "{\"name\":\"Persian\"},{\"name\":\"Burmese\"},{\"name\":\"Siberian\"}," +
                "{\"name\":\"Balinese\"},{\"name\":\"Russian Blue\"},{\"name\":\"Maine Coon\"}]," +
                "\"features\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\"," +
                "\"name\":\"ClarabridgeRule\",\"parameters\":{\"label_rules\":" +
                "\"{\\\"American Short Hair\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]],\\\"Siamese\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," +
                "\\\"Persian\\\":[[\\\"\\\",\\\"\\\",\\\"white\\\",\\\"\\\"]]," + 
                "\\\"Burmese\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]],\\\"Siberian\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," +
                "\\\"Balinese\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]],\\\"Russian Blue\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]," +
                "\\\"Maine Coon\\\":[[\\\"\\\",\\\"\\\",\\\"\\\",\\\"\\\"]]}\"},\"is_active\":true}]}}";
        
        JsonObject taskJson = Json.createReader(new StringReader(json)).readObject();
        Collection mockCollection = Collection.instance(null, "C");
        Task mockTask = Task.instance(mockCollection, taskJson);
        
        java.lang.reflect.Method method = Task.class.getDeclaredMethod("isTrivialAccept");
        method.setAccessible(true);

        assertThat((boolean)method.invoke(mockTask), is(false));
    }
}
