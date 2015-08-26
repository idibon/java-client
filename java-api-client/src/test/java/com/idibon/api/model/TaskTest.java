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
        	"{\"American Short Hair\":[[\"\",\"\",\"\",\"\"]],\"Siamese\":[[\"\",\"\",\"\",\"\"]]," +
        	"\"Persian\":[[\"\",\"\",\"\",\"\"]]," + 
        	"\"Burmese\":[[\"\",\"\",\"\",\"\"]],\"Siberian\":[[\"\",\"\",\"\",\"\"]]," +
        	"\"Balinese\":[[\"\",\"\",\"\",\"\"]],\"Russian Blue\":[[\"\",\"\",\"\",\"\"]]," +
        	"\"Maine Coon\":[[\"\",\"\",\"\",\"\"]]}},\"is_active\":true}]}}";
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
        	assert(labels.get(label).equals("1.0"));
        }
        
        // Ensure that there are no features
        assert(predictionJson.getJsonObject(0).get("features") == null);
        
        // Specify that we want to see features, then ensure that an empty feature object is included
        prediction = prediction.withSignificantFeatures();
        assert(predictionJson.getJsonObject(0).get("features") != null);
        assert(predictionJson.getJsonObject(0).get("features").toString().equals("{}"));
    }
    
    // TODO: @Test public void testNontrivialRules() throws Exception { }
}
