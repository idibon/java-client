/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;
import com.idibon.api.util.Either;

import static com.idibon.api.util.Adapters.wrapCharSequence;
import static com.idibon.api.util.Adapters.wrapCharSequences;

/**
 * Takes a string from the command line and predicts it against the
 * EnglishSocialSentiment task using an Idibon Public server.
 */
public class PredictIdibonPublic
{
    final static String COLLECTION_NAME = "Idibon";
    final static String TASK_NAME = "EnglishSocialSentiment";

    /**
     * Classifies a string of content using a task on the Idibon Public server,
     * and prints the result to the console.
     *
     * @param task The task (AI model) that will perform the classification
     * @param content The content that should be classified
     */
    public static void makeDocumentPrediction(Task task, String content)
          throws Exception {

        Iterable<DocumentContent> streamDocuments =
            wrapCharSequences(Arrays.asList(content));

        // Drop the prediction threshold to 0.6 to pick up more features
        PredictionIterable<DocumentPrediction> predictedResults =
            task.classifications(streamDocuments).withSignificantFeatures(0.6);

        for (Either<APIFailure<DocumentContent>, DocumentPrediction> pred : predictedResults) {
            if (pred.isLeft()) throw pred.left.exception;
            DocumentPrediction p = pred.right;
            Map<Label, Double> confidence = p.getPredictedConfidences();

            for (Map.Entry<Label, Double> e : confidence.entrySet())
                System.out.printf("%s: %f\n", e.getKey().getName(), e.getValue());
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.printf("Usage: %s Content...\n",
                              PredictIdibonPublic.class.getSimpleName());
            return;
        }

        // connect to the local Idibon Public server, running on port 8080
        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface().forServer("http://localhost:8080"));

        // Get a reference to the configured task
        Task task = client.collection(COLLECTION_NAME).task(TASK_NAME);

        /* Join all of the strings on the command line into one, in case the
         * user didn't put the entire block in quotes. */
        String content = "";
        for (int i = 0; i < args.length; i++) {
            if (!content.isEmpty()) content += " ";
            content += args[i];
        }

        try {
            makeDocumentPrediction(task, content);
        } finally {
            client.shutdown(0);
        }
    }
}
