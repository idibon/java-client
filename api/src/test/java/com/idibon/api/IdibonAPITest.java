/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api;

import java.io.IOException;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;

import com.google.gson.*;

import org.junit.*;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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

    @AfterClass public static void shutdown() {
        _apiClient.shutdown(0);
    }
}
