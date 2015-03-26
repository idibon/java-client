/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.io.IOException;
import java.util.*;

import com.idibon.api.http.HttpException;
import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.model.Collection;
import com.idibon.api.util.Either;

import javax.json.*;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.idibon.api.util.Adapters.buildAnnotations;
import static com.idibon.api.util.Adapters.flattenRight;
import static com.idibon.api.model.DocumentSearcher.ReturnData.*;
import static com.idibon.api.model.DocumentAnnotationQuery.forTasks;

public class IdibonAPI_IT {

    private static IdibonAPI _apiClient;

    @BeforeClass public static void configure() throws Exception {
        String apiKey = System.getenv("IDIBON_API_KEY");
        if (apiKey == null)
            throw new NullPointerException("Missing IDIBON_API_KEY");

        String apiTarget = System.getenv("IDIBON_API");
        if (apiTarget == null || apiTarget.isEmpty())
            apiTarget = "https://api.idibon.com/";

        _apiClient = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer(apiTarget).withApiKey(apiKey));
    }

    @Test public void canLazyLoadCollection() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        JsonObject info = collection.getJson();
        assertThat(info.getString("name"), equalTo("DemoOfTesla"));
    }

    @Test public void canReadDocumentNames() throws Exception {
        // tests the non-streaming, name-only mode
        Collection collection = _apiClient.collection("DemoOfTesla");
        int count = 0;
        for (Either<IOException, Document> d : collection.documents()) {
            if (d.isLeft()) throw d.left;
            assertThat(d.right.isLoaded(), is(false));
            count++;
        }
        assertThat(count, is(75113));
    }

    @Test public void canReadDocumentContent() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        int count = 0;
        Iterator<Either<IOException, Document>> it = collection.documents()
            .returning(DocumentContent).iterator();
        JsonObject first = it.next().right.getJson();

        assertThat(first.getString("content", null), is(not(nullValue())));
    }

    @Test public void canStreamDocuments() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        int count = 0;
        for (Either<IOException, Document> d : collection.documents().returning(AllAnnotations)) {
            if (d.isLeft()) throw d.left;
            JsonValue anns = d.right.getJson().get("annotations");
            assertThat(anns, either(is(instanceOf(JsonArray.class))).or(is(nullValue())));
            count++;
            if (count >= 3000) break;
        }
        assertThat(count, is(3000));
    }

    @Test public void canReadAnnotations() throws Exception {
        Collection collection = _apiClient.collection("general_sentiment_5pt_scale");
        for (Either<IOException, Document> doc : collection.documents().returning(TaskAnnotations)
                 .annotated(forTasks("NP_Chunking")).first(5)) {
            for (Annotation ann : doc.right.getAnnotations()) {
                assertThat(ann, is(instanceOf(Annotation.SpanAssignment.class)));
                Annotation.SpanAssignment assign = (Annotation.SpanAssignment)ann;
                assertThat(assign.label.getName(), is("NP-Chunk"));
            }
        }
    }

    @Test public void canLimitDocuments() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        for (int limit = 1; limit <= 5; limit++) {
            List<Document> result = new ArrayList<Document>();
            for (Either<IOException, Document> d : collection.documents().first(limit)) {
                if (d.isLeft()) throw d.left;
                result.add(d.right);
            }
            assertThat(result, hasSize(limit));
        }
    }

    @Test public void canSkipUnwantedResults() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        List<String> expectedNames = new ArrayList<String>();
        Set<String> uniqueNames = new HashSet<String>();

        for (Either<IOException, Document> d : collection.documents().first(5)) {
            if (d.isLeft()) throw d.left;
            expectedNames.add(d.right.getName());
            uniqueNames.add(d.right.getName());
        }

        assertThat(uniqueNames, hasSize(5));

        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            for (Either<IOException, Document> d : collection.documents().first().ignoring(i)) {
                if (d.isLeft()) throw d.left;
                names.add(d.right.getName());
            }
        }

        assertThat(names, equalTo(expectedNames));
    }

    @Test public void canMakePredictions() throws Exception {
        Collection c = _apiClient.collection("general_sentiment_5pt_scale");
        Task sentiment = c.task("Sentiment");
        List<String> predicted = new ArrayList<String>();
        for (Either<IOException, DocumentPrediction> p : sentiment
                 .classifications(flattenRight(c.documents().first(100)))) {
            if (p.isLeft()) throw p.left;
            predicted.add(p.right.getRequestedAs(Document.class).getName());
            assertThat(p.right.getPredictedConfidences().size(), is(5));
        }
        List<String> expected = new ArrayList<String>();
        for (Either<IOException, Document> d : c.documents().first(100)) {
            if (d.isLeft()) throw d.left;
            expected.add(d.right.getName());
        }

        assertThat(predicted, is(expected));
    }

    @Test public void predictionsCanIncludeFeatures() throws Exception {
        Collection c = _apiClient.collection("general_sentiment_5pt_scale");
        Task sentiment = c.task("Sentiment");
        PredictionIterable<DocumentPrediction> predictions = sentiment
            .classifications(flattenRight(c.documents().first()));

        // first, try without specifying a feature threshold.
        for (Either<IOException, DocumentPrediction> p : predictions) {
            if (p.isLeft()) throw p.left;
            assertThat(p.right.getSignificantFeatures(), is(nullValue()));
        }

        for (Either<IOException, DocumentPrediction> p : predictions.withSignificantFeatures(0.5)) {
            if (p.isLeft()) throw p.left;
            Map<Label, List<String>> features = p.right.getSignificantFeatures();
            assertThat(features, is(not(nullValue())));
            assertThat(features.size(), is(not(0)));
        }
    }

    @Test public void canUploadAndDeleteDocuments() throws Exception {
        Collection c = _apiClient.collection("e0db414891d6-test124");
        List<DocumentContent.Named> content = new ArrayList<>();

        try {
            content.add(UploadableDoc.named("homer simpson").content("d'oh!"));
            content.add(UploadableDoc.named("bart simpson").content("eat my shorts!"));
            c.addDocuments(content);

            // verify that the documents were actually uploaded
            Document homer = c.document("homer simpson");
            Document bart = c.document("bart simpson");

            assertThat(homer.getContent(), is("d'oh!"));
            assertThat(bart.getContent(), is("eat my shorts!"));

        } finally {
            _apiClient.waitFor(c.document("homer simpson").deleteAsync(),
                               c.document("bart simpson").deleteAsync());
        }

        try {
            c.document("homer simpson").getContent();
            throw new RuntimeException("Document was not deleted");
        } catch (HttpException.NotFound _) {
            // ignore. the document should not be found
        }
    }

    @Test public void canCommitAndDeleteAnnotations() throws Exception {
        Collection c = _apiClient.collection("general_sentiment_5pt_scale");
        c.addDocuments(Arrays.asList(
            UploadableDoc.named("homer simpson").content("d'oh")));
        Document homer = c.document("homer simpson");
        try {
            Label l = c.task("Sentiment").label("Negative3");
            c.commitAnnotations(buildAnnotations(Arrays.asList(
                homer.createAssignment(l)
                    .provenance(Annotation.Provenance.Human)
                    .is(AnnotationBuilder.Assignment.Status.Valid)))
            );
            List<? extends Annotation> anns =
                homer.invalidate().getAnnotations();
            assertThat(anns, hasSize(1));
            Annotation check = anns.get(0);
            assertThat(check, is(instanceOf(Annotation.DocumentAssignment.class)));
            Annotation.DocumentAssignment assign = (Annotation.DocumentAssignment)check;
            assertThat(assign.label, is(l));
            assertThat(assign.document, is(homer));

            _apiClient.waitFor(assign.deleteAsync());
            anns = homer.invalidate().getAnnotations();
            assertThat(anns, is(empty()));
        } finally {
            _apiClient.waitFor(homer.deleteAsync());
        }
    }

    @Test public void canAddAndDeleteTuningRules() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        Task task = c.task("Sentiment");
        Label label = task.label("Neutral0");
        TuningRules.Rule rule = label.createRule("hiybbprqag", 0.75);
        task.addRules(rule);
        try {
            assertThat(label.getRules(), hasSize(1));
            assertThat(label.getRules().get(0).phrase, is(rule.phrase));
            assertThat(label.getRules().get(0).weight, is(rule.weight));
            TuningRules.Rule update = label.createRule(rule.phrase, 0.95);
            task.addRules(update);
            assertThat(label.getRules(), hasSize(1));
            assertThat(label.getRules().get(0).phrase, is(update.phrase));
            assertThat(label.getRules().get(0).weight, is(update.weight));
            // should be a no-op, since the weights don't match
            task.deleteRules(rule);
            assertThat(label.getRules(), hasSize(1));
            // update which rule will be deleted in the finally block
            rule = update;
        } finally {
            task.deleteRules(rule);
        }
        /* move this outside the try, so that it doesn't clobber an
         * earlier exception */
        assertThat(label.getRules(), is(empty()));
    }

    @Test public void canAddAndRemoveSubtasks() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        Task task = c.task("Relevance");
        Label label = task.label("Relevant");
        Task subtask = c.task("Sentiment");
        task.addSubtaskTriggers(label, subtask);
        try {
            Map<Label, Set<? extends Task>> ontology = task.getSubtasks();
            assertThat(ontology, hasKey(label));
            assertThat(ontology.get(label), contains(subtask));
        } finally {
            task.deleteSubtaskTriggers(label, subtask);
        }
        assertThat(task.getSubtasks().keySet(), is(empty()));
    }

    @SuppressWarnings("unchecked")
    @Test public void canAddAndRemoveLabels() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        Task task = c.task("Relevance");
        String desc = "This label is garbage";
        Label garbage = task.createLabel("Garbage")
            .setDescription(desc).commit();

        try {
            assertThat(task.label("Garbage"), is(sameInstance(garbage)));
            assertThat((List<Label>)(Object)task.getLabels(), hasItem(garbage));
            assertThat(garbage.getDescription(), is(desc));
        } finally {
            garbage.delete();
        }

        assertThat((List<Label>)(Object)task.getLabels(), not(hasItem(garbage)));
    }

    @SuppressWarnings("unchecked")
    @Test public void automaticallyRenamesRulesAndSubtasks() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        Task task = c.task("Relevance");
        Label garbage = task.createLabel("Garbage").commit();

        try {
            Label backup = garbage;
            task.addRules(garbage.createRule("hiybbprqag", 0.75));
            task.addSubtaskTriggers(garbage, c.task("Sentiment"));
            garbage = garbage.modify().setName("GarbageGarbage").commit();
            assertThat(garbage.getName(), is("GarbageGarbage"));
            assertThat(task.getSubtasks(), hasKey(garbage));
            assertThat(task.getSubtasks(), not(hasKey(backup)));
            assertThat((Set<Task>)(Object)task.getSubtasks().get(garbage),
                       hasItem(c.task("Sentiment")));
            assertThat(garbage.getRules(), hasSize(1));
            assertThat(garbage.getRules().get(0).phrase, is("hiybbprqag"));
        } finally {
            garbage.delete();
        }

        assertThat(task.getSubtasks().keySet(), is(empty()));
    }

    @Test public void canAddAndRemoveTasks() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        Task snowman = c.createTask(Task.Scope.document, "Hi ☃")
            .setDescription("It's U+2603")
            .addLabel("Snowman")
            .addLabel("Frosty")
            .disallowTraining()
            .commit();
        try {
            c.invalidate();
            assertThat(c.getAllTasks(), hasItem(snowman));
        } finally {
            snowman.delete();
        }
        c.invalidate();
        assertThat(c.getAllTasks(), not(hasItem(snowman)));
    }

    @Test public void automaticallyRenamesSubtasks() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        Task parent = c.createTask(Task.Scope.document, "Parent Task")
            .addLabel("Trigger").commit();
        Label trigger = parent.label("Trigger");

        try {
            Task child = c.createTask(Task.Scope.document, "Child task").commit();
            try {
                parent.addSubtaskTriggers(trigger, child);
                assertThat(trigger.getSubtasks(), hasItem(child));
                Task sibling = child;
                child = child.modify().setName("Sibling").commit();
                assertThat(child.getName(), is("Sibling"));
                assertThat(trigger.getSubtasks(), not(hasItem(sibling)));
                assertThat(trigger.getSubtasks(), hasItem(child));
            } finally {
                child.delete();
            }
            assertThat(trigger.getSubtasks(), not(hasItem(child)));
        } finally {
            parent.delete();
        }
    }

    @Test public void distinguishesRootTasksFromAllTasks() throws Exception {
        Collection c = _apiClient.collection("zest_zest");
        assertThat(c.getRootTasks(), is(c.getAllTasks()));

        Task parent = c.createTask(Task.Scope.document, "Parent Task")
            .addLabel("Trigger").commit();
        Label trigger = parent.label("Trigger");

        try {
            Task child = c.createTask(Task.Scope.document, "Child task").commit();
            try {
                assertThat(c.getRootTasks(), hasItems(parent, child));
                parent.addSubtaskTriggers(trigger, child);
                assertThat(c.getRootTasks(), hasItem(parent));
                assertThat(c.getRootTasks(), not(hasItem(child)));
                assertThat(c.getRootTasks(), is(not(c.getAllTasks())));
            } finally {
                child.delete();
            }
        } finally {
            parent.delete();
        }
    }

    @AfterClass public static void shutdown() {
        _apiClient.shutdown(0);
    }

    private static class UploadableDoc implements DocumentContent.Named {
        public UploadableDoc name(String name) {
            _name = name;
            return this;
        }

        public UploadableDoc content(String content) {
            _content = content;
            return this;
        }

        public String getName() { return _name; }
        public String getContent() { return _content; }
        public JsonObject getMetadata() { return _metadata; }

        private String _name = null;
        private String _content = null;
        private JsonObject _metadata = null;

        public static UploadableDoc named(String name) {
            return new UploadableDoc().name(name);
        }
    }
}
