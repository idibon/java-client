/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileReader;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;
import javax.json.*;

/**
 * Example application which uploads one or more JSON files with the following
 * object format:
 * <pre>
 * {
 *   "name":"Document name",
 *   "content":"Document content",
 *   "metadata":{ ... }
 * }
 * </pre>
 * The <tt>name</tt> key is optional. If not provided, the API will assign an
 * internally-generated name.
 */
public class UploadJsonDocuments
{
    /**
     * Documents parsed from *.json files with content and metadata keys.
     */
    public static class JsonDocument implements DocumentContent {
        public JsonDocument(JsonObject json) {
            _json = json;
        }

        public String getContent() {
            return _json.getString("content");
        }

        public JsonObject getMetadata() {
            return _json.getJsonObject("metadata");
        }

        protected final JsonObject _json;
    }

    /**
     * Documents parsed from *.json files that have user-defined name keys.
     */
    public static class NamedJsonDocument extends JsonDocument
          implements DocumentContent.Named {
        public NamedJsonDocument(JsonObject json) {
            super(json);
        }

        public String getName() {
            return _json.getString("name");
        }
    }

    public static List<JsonDocument> readContent(String[] files) throws Exception {
        List<JsonDocument> list = new ArrayList<>();

        for (String filename : files) {
            try(JsonReader reader = Json.createReader(new FileReader(filename))) {
                JsonObject doc = reader.readObject();
                if (doc.getString("name", "").isEmpty())
                    list.add(new JsonDocument(doc));
                else
                    list.add(new NamedJsonDocument(doc));
            }
        }
        return list;
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.printf("Usage: %s API_KEY COLLECTION FILES...\n",
                              UploadJsonDocuments.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        Collection collection = client.collection(args[1]);

        String[] jsonFiles = Arrays.copyOfRange(args, 2, args.length);

        List<JsonDocument> docsToPost = readContent(jsonFiles);

        long start = System.currentTimeMillis();
        collection.addDocuments(docsToPost);
        long end = System.currentTimeMillis();

        client.shutdown(0);

        double elapsed = (end - start) / 1000.0;
        System.out.printf("Uploaded %d documents in %.2fs\n",
                          docsToPost.size(), elapsed);

    }
}
