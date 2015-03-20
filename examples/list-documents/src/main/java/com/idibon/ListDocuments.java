/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.Arrays;
import java.io.IOException;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;
import com.idibon.api.util.Either;

import static com.idibon.api.model.DocumentAnnotationQuery.forTasks;

/**
 * Simple Idibon SDK example app listing the documents in a collection,
 * optionally only those documents that have annotations on specific tasks.
 */
public class ListDocuments
{
    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.printf("Usage: %s API_KEY COLLECTION [TASK]\n",
                              ListDocuments.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        DocumentSearcher documents = client.collection(args[1]).documents();

        if (args.length > 2) {
            /* this can search for any number of tasks, but the example
             * is just the one task on the command line */
            documents = documents.annotated(forTasks(args[2]));
        }

        long count = 0;
        for (Either<IOException, Document> doc : documents) {
            if (doc.isLeft()) throw doc.left;
            System.out.printf("%s\n", doc.right.getJson().getString("name"));
            count++;
        }

        System.out.printf("%d results\n", count);

        client.shutdown(0);
    }
}
