/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;

import static com.idibon.api.util.Adapters.wrapCharSequences;

/**
 * Takes a string from the command line and predicts it against a task.
 */
public class PredictContent
{
    /**
     * Generates a streaming prediction for the provided content, and
     * reports all of the per-label confidences and significant features.
     */
    public static void makePrediction(Task task, String content)
          throws Exception {

        Iterable<DocumentContent> streamDocuments =
            wrapCharSequences(Arrays.asList(content));

        Iterable<DocumentPrediction> predictedResults =
            task.classifications(streamDocuments).withSignificantFeatures(0.6);

        for (DocumentPrediction p : predictedResults) {
            Map<Label, Double> confidence = p.getPredictedConfidences();
            Map<Label, List<String>> features = p.getSignificantFeatures();
            if (features == null)
                features = Collections.<Label, List<String>>emptyMap();

            for (Map.Entry<Label, Double> e : confidence.entrySet()) {
                String result = e.getKey().getName() + ": " + e.getValue();

                List<String> sigFeats = features.get(e.getKey());
                if (sigFeats != null) {
                    result += " [ ";
                    for (String f : sigFeats) result += f + ", ";
                    result += "]";
                }
                System.out.printf("%s\n", result);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.out.printf("Usage: %s API_KEY COLLECTION TASK Content...\n",
                              PredictContent.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        Task task = client.collection(args[1]).task(args[2]);

        /* Join all of the strings on the command line into one, in case the
         * user didn't put the entire block in quotes. */
        String content = "";
        for (int i = 3; i < args.length; i++) {
            if (!content.isEmpty()) content += " ";
            content += args[i];
        }

        try {
            makePrediction(task, content);
        } finally {
            client.shutdown(0);
        }
    }
}