/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.io.IOException;
import java.util.Iterator;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;

import com.google.gson.*;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static com.idibon.api.model.DocumentSearcher.ReturnData.*;

public class IdibonAPITest {

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
        Collection collection = _apiClient.getCollection("DemoOfTesla");
        JsonObject info = collection.getJson();
        assertThat(info.getAsJsonPrimitive("name").getAsString(), equalTo("DemoOfTesla"));
    }

    @Test public void canReadDocumentNames() throws Exception {
        // tests the non-streaming, name+date-only mode
        Collection collection = _apiClient.getCollection("DemoOfTesla");
        int count = 0;
        for (Document d : collection.documents()) {
            assertThat(d.getJson().getAsJsonPrimitive("content"), is(nullValue()));
            assertThat(d.getJson().getAsJsonPrimitive("created_at"), is(not(nullValue())));
            count++;
        }
        assertThat(count, is(75113));
    }

    @Test public void canReadDocumentContent() throws Exception {
        Collection collection = _apiClient.getCollection("DemoOfTesla");
        int count = 0;
        Iterator<Document> it = collection.documents()
            .returning(DocumentContent).iterator();
        JsonObject first = it.next().getJson();

        assertThat(first.getAsJsonPrimitive("content"), is(instanceOf(JsonPrimitive.class)));
    }

    @Test public void canStreamDocuments() throws Exception {
        Collection collection = _apiClient.getCollection("DemoOfTesla");
        int count = 0;
        for (Document d : collection.documents().returning(AllAnnotations)) {
            JsonElement anns = d.getJson().get("annotations");
            assertThat(anns, either(is(instanceOf(JsonArray.class))).or(is(nullValue())));
            count++;
        }
        assertThat(count, is(75113));
    }

    @AfterClass public static void shutdown() {
        _apiClient.shutdown(0);
    }
}
