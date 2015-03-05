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

import javax.json.*;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.idibon.api.model.DocumentSearcher.ReturnData.*;

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
        for (Document d : collection.documents()) {
            assertThat(d.isLoaded(), is(false));
            count++;
        }
        assertThat(count, is(75113));
    }

    @Test public void canReadDocumentContent() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        int count = 0;
        Iterator<Document> it = collection.documents()
            .returning(DocumentContent).iterator();
        JsonObject first = it.next().getJson();

        assertThat(first.getString("content", null), is(not(nullValue())));
    }

    @Test public void canStreamDocuments() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        int count = 0;
        for (Document d : collection.documents().returning(AllAnnotations)) {
            JsonValue anns = d.getJson().get("annotations");
            assertThat(anns, either(is(instanceOf(JsonArray.class))).or(is(nullValue())));
            count++;
            if (count >= 3000) break;
        }
        assertThat(count, is(3000));
    }

    @Test public void canLimitDocuments() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        for (int limit = 1; limit <= 5; limit++) {
            List<Document> result = new ArrayList<Document>();
            for (Document d : collection.documents().first(limit))
                result.add(d);
            assertThat(result, hasSize(limit));
        }
    }

    @Test public void canSkipUnwantedResults() throws Exception {
        Collection collection = _apiClient.collection("DemoOfTesla");
        List<String> expectedNames = new ArrayList<String>();
        Set<String> uniqueNames = new HashSet<String>();

        for (Document d : collection.documents().first(5)) {
            expectedNames.add(d.getName());
            uniqueNames.add(d.getName());
        }

        assertThat(uniqueNames, hasSize(5));

        List<String> names = new ArrayList<String>();
        for (int i = 0; i < 5; i++) {
            for (Document d : collection.documents().first().ignoring(i))
                names.add(d.getName());
        }

        assertThat(names, equalTo(expectedNames));
    }

    @Test public void canMakePredictions() throws Exception {
        Collection c = _apiClient.collection("general_sentiment_5pt_scale");
        Task sentiment = c.task("Sentiment");
        List<String> predicted = new ArrayList<String>();
        for (DocumentPrediction p : sentiment
                 .classifications(c.documents().first(100))) {
            predicted.add(p.getRequestedAs(Document.class).getName());
            assertThat(p.getPredictedConfidences().size(), is(5));
        }
        List<String> expected = new ArrayList<String>();
        for (Document d : c.documents().first(100))
            expected.add(d.getName());

        assertThat(predicted, is(expected));
    }

    @Test public void predictionsCanIncludeFeatures() throws Exception {
        Collection c = _apiClient.collection("general_sentiment_5pt_scale");
        Task sentiment = c.task("Sentiment");
        PredictionIterable<DocumentPrediction> predictions = sentiment
            .classifications(c.documents().first());

        // first, try without specifying a feature threshold.
        for (DocumentPrediction p : predictions)
            assertThat(p.getSignificantFeatures(), is(nullValue()));

        for (DocumentPrediction p : predictions.withSignificantFeatures(0.5)) {
            Map<Label, List<String>> features = p.getSignificantFeatures();
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
