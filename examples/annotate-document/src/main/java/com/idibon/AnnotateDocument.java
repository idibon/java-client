/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.Arrays;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;

import static com.idibon.api.util.Adapters.buildAnnotations;

/**
 * Simple Idibon SDK example app listing the documents in a collection,
 * optionally only those documents that have annotations on specific tasks.
 */
public class AnnotateDocument
{
    public static void main(String[] args) throws Exception {

        if (args.length != 5) {
            System.out.printf("Usage: %s API_KEY COLLECTION DOCUMENT_NAME " +
                "TASK_NAME LABEL_NAME\n", AnnotateDocument.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        Collection collection = client.collection(args[1]);

        Document documentToUpdate = collection.document(args[2]);

        Label labelToAssign = collection.task(args[3]).label(args[4]);

        // Create and commit the new annotation
        collection.commitAnnotations(buildAnnotations(Arrays.asList(
            documentToUpdate.createAssignment(labelToAssign)
              .provenance(Annotation.Provenance.Human)
              .is(AnnotationBuilder.Assignment.Status.Valid)
        )));

        System.out.printf("Added annotation for TASK='%s' LABEL='%s' to '%s'\n",
            labelToAssign.getTask().getName(), labelToAssign.getName(),
            documentToUpdate.getName());

        client.shutdown(0);
    }
}
