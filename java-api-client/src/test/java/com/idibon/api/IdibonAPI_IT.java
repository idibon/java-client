/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.io.IOException;
import java.util.*;

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
        // tests the non-streaming, name+date-only mode
        Collection collection = _apiClient.collection("DemoOfTesla");
        int count = 0;
        for (Document d : collection.documents()) {
            assertThat(d.getJson().getString("content", null), is(nullValue()));
            assertThat(d.getJson().getString("created_at"), is(not(nullValue())));
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
        for (DocumentPrediction p :
                 sentiment.classifications(c.documents().first(100))) {
            predicted.add(p.getPredictableAs(Document.class).getName());
        }
        List<String> expected = new ArrayList<String>();
        for (Document d : c.documents().first(100))
            expected.add(d.getName());

        assertThat(predicted, is(expected));
    }

    @AfterClass public static void shutdown() {
        _apiClient.shutdown(0);
    }
}
