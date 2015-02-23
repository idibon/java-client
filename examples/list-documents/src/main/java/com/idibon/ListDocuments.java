/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.Arrays;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;

import static com.idibon.api.model.DocumentAnnotationQuery.forTasks;

/**
 * Simple Idibon SDK example app listing the documents in a collection,
 * optionally only those documents that have annotations on specific tasks.
 */
public class ListDocuments
{
    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.printf("Usage: %s API_KEY COLLECTION TASKS...\n",
                              ListDocuments.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        DocumentSearcher documents = client.collection(args[1]).documents();

        if (args.length > 2) {
            String[] annotatedTasks = Arrays.copyOfRange(args, 2, args.length);
            documents = documents.annotated(forTasks(annotatedTasks));
        }

        long count = 0;
        for (Document doc : documents) {
            System.out.printf("%s\n", doc.getJson().getString("name"));
            count++;
        }

        System.out.printf("%d results\n", count);

        client.shutdown(0);
    }
}
