/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon;

import java.util.*;
import java.io.IOException;

import com.idibon.api.http.impl.JdkHttpInterface;
import com.idibon.api.model.*;
import com.idibon.api.model.Collection;
import com.idibon.api.IdibonAPI;
import com.idibon.api.util.Either;

import static com.idibon.api.model.DocumentAnnotationQuery.forTasks;

/**
 * Simple Idibon SDK example app listing the documents in a collection,
 * optionally only those documents that have annotations on specific tasks.
 */
public class PrintOntology
{
    public static void printTaskOntology(Task node, Deque<Task> ancestors) throws Exception {
        String indent = "";
        for (int i = 0; i < ancestors.size(); i++) indent += "    ";
        System.out.printf("%s -> %s\n", indent, node.getName());

        ancestors.addLast(node);
        indent += "    ";
        for (Label label : node.getLabels()) {
            System.out.printf("%s%s\n", indent, label.getName());
            for (Task subtask : label.getSubtasks()) {
                if (ancestors.contains(subtask)) {
                    System.out.printf("%s -> %s [CIRCULAR]\n", indent,
                                      label.getName(), subtask.getName());
                } else {
                    printTaskOntology(subtask, ancestors);
                }
            }
        }

        ancestors.removeLast();
    }

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.printf("Usage: %s API_KEY COLLECTION [TASK]\n",
                              PrintOntology.class.getSimpleName());
            return;
        }

        IdibonAPI client = new IdibonAPI()
            .using(new JdkHttpInterface()
                   .forServer("https://api.idibon.com")
                   .withApiKey(args[0]));

        try {
            Collection collection = client.collection(args[1]);

            List<Task> roots = args.length == 3
                ? Arrays.asList(collection.task(args[2]))
                : collection.getRootTasks();

            for (Task rootTask : roots)
                printTaskOntology(rootTask, new LinkedList<Task>());

        } finally {
            client.shutdown(0);
        }
    }
}
