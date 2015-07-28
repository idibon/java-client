/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.IdibonAPI;
import com.idibon.api.util.Either;

import static com.idibon.api.model.DocumentAnnotationQuery.forTasks;

/**
 * Simple Idibon SDK example app that shows all of the tasks in a collection,
 * and their associated subtasks, updated since a time specified using an
 * ISO-8601 string provided on the command-line
 */
public class ShowUpdatedTasks
{
    private static boolean isUpdatedSince(Task task, Date since) {
        try {
            return task.getDate(Task.Keys.updated_at).compareTo(since) > 0;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to compare update times", ex);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.printf("Usage: %s API_KEY COLLECTION UPDATE-SINCE\n",
                              ShowUpdatedTasks.class.getSimpleName());
            return;
        }

        SimpleDateFormat iso8601Parser =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        iso8601Parser.setTimeZone(TimeZone.getTimeZone("UTC"));

        final Date since = iso8601Parser.parse(args[2]);

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        try {
            /* grab every task in the collection that has been updated after
             * the time on the command-line */
            List<Task> updatedTasks = client.collection(args[1])
                .getAllTasks().stream()
                .filter(task -> isUpdatedSince(task, since))
                .collect(Collectors.toList());

            for (Task t : updatedTasks)
                System.out.printf("%s: updated at %s\n", t.getName(),
                    iso8601Parser.format(t.getDate(Task.Keys.updated_at)));
        } finally {
            client.shutdown(0);
        }
    }
}
