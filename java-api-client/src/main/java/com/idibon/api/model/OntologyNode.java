/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;
import java.io.IOException;
import javax.json.*;

import com.idibon.api.model.Collection;

/**
 * Stores the subtask graph for a single task.
 */
class OntologyNode
      extends HashMap<Label, Set<Task>>
      implements Cloneable {

    // The key in the task config hash with subtask layout
    static final String CONFIG_SUBTASK_KEY = "sub_tasks";

    public Set<Task> getOrDefault(Label label) {
        Set<Task> existing = get(label);
        if (existing == null) {
            existing = new HashSet<>();
            put(label, existing);
        }
        return existing;
    }

    /**
     * See {@link java.lang.Cloneable#clone}.
     */
    @Override public OntologyNode clone() {
        OntologyNode result = new OntologyNode();
        for (Map.Entry<Label, Set<Task>> entry : entrySet()) {
            Set<Task> subtaskCopy = new HashSet<>(entry.getValue());
            result.put(entry.getKey(), subtaskCopy);
        }
        return result;
    }

    /**
     * Reads the JSON subtask hash (labelName => [ task list ]) into a
     * {@link java.util.Map}<{@link com.idibon.api.model.Label},
     * {@link java.util.List}<{@link com.idibon.api.model.Task}>>
     *
     * @param task The task where this ontology node is located
     * @param json The raw JSON configuration data (<tt>task[:config]</tt>),
     *        possibly null.
     */
    static OntologyNode parse(Task task, JsonObject configData) {

        if (configData == null) return new OntologyNode();
        JsonObject json = configData.getJsonObject(CONFIG_SUBTASK_KEY);
        OntologyNode node = new OntologyNode();
        if (json == null) return node;

        /* loop over all the label names in the JSON, grab the tasks
         * triggered by each. */
        for (String label : json.keySet()) {
            JsonArray subtasks = json.getJsonArray(label);
            Set<Task> list = new HashSet<>(subtasks.size());
            for (JsonString name : subtasks.getValuesAs(JsonString.class))
                list.add(task.getCollection().task(name.getString()));

            node.put(task.label(label), list);
        }
        return node;
    }
}
