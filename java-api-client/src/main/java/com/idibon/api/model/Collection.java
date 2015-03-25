/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.*;

import com.idibon.api.http.*;
import com.idibon.api.util.Either;
import com.idibon.api.util.Memoize;
import com.idibon.api.model.Collection;
import javax.json.*;

import static com.idibon.api.model.Util.JSON_BF;
import static com.idibon.api.model.OntologyNode.CONFIG_SUBTASK_KEY;

/**
 * The Collection is the top-most item in an analysis project.
 *
 * A Collection consists of Documents and Tasks.
 */
public class Collection extends IdibonHash {

    /**
     * Keys that may appear in the raw JSON hash.
     */
    public enum Keys {
        /**
         * Arbitrary configuration data (<tt>JsonObject</tt>).
         */
        config,
        /**
         * Date the collection was created (<tt>ISO-8601 String</tt>).
         */
        created_at,
        /**
         * User-friendly description of the collection (<tt>String</tt>).
         */
        description,
        /**
         * Reserved (<tt>Boolean</tt>).
         */
        is_active,
        /**
         * Reserved (<tt>Boolean</tt>)
         */
        is_public,
        /**
         * Name of the collection (<tt>String</tt>).
         */
        name,
        /**
         * Reserved (<tt>JsonArray</tt>).
         */
        streams,
        /**
         * UUID of the subscriber who created the collection
         * (<tt>UUID String</tt>).
         */
        subscriber_id,
        /**
         * All of the tasks in this collection (<tt>JsonArray</tt>).
         */
        tasks,
        /**
         * Date of most recent change to the collection or any task in it
         * (<tt>ISO-8601 String</tt>).
         */
        touched_at,
        /**
         * Date the collection was most last changed (<tt>ISO-8601 String</tt>).
         */
        updated_at,
        /**
         * UUID of the collection (<tt>UUID String</tt>).
         */
        uuid;
    }

    /**
     * Returns the raw JSON data for this Collection
     */
    @Override public JsonObject getJson() throws IOException {
        return super.getJson(null).getJsonObject("collection");
    }

    /**
     * Returns the value in the JSON hash for the specified key.
     *
     * @param key The value in the JSON hash to return
     * @return The value in the hash at key
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonValue> T get(Keys key) throws IOException {
        return (T)getJson().get(key.name());
    }

    /**
     * Returns the tasks defined for this collection.
     */
    public List<? extends Task> getTasks() throws IOException {
        JsonArray taskArray = get(Keys.tasks);
        List<Task> tasks = new ArrayList<>(taskArray.size());
        for (JsonObject taskJson : taskArray.getValuesAs(JsonObject.class))
            tasks.add(task(taskJson.getString(Task.Keys.name.name())));
        return tasks;
    }

    /**
     * Returns a DocumentSearcher instance to search for specific documents in
     * this collection.
     */
    public DocumentSearcher documents() {
        return new DocumentSearcher(this, _httpIntf);
    }

    /**
     * Returns a Document instance for a document with the given name.
     */
    public Document document(String name) {
        return Document.instance(this, name);
    }

    /**
     * Returns a Document instance for the provided JSON document hash
     */
    public Document document(JsonObject docJson) {
        return Document.instance(this, docJson);
    }

    /**
     * Uploads new content to the API
     *
     * The upload will terminate if an error is encountered.
     *
     * @param documents The list of new documents that should be uploaded
     */
    public void addDocuments(Iterable<? extends DocumentContent> documents)
          throws IOException {
        addDocuments(documents.iterator());
    }

    /**
     * Uploads new content to the API
     *
     * The upload will terminate if an error is encountered.
     *
     * @param docs The list of new documents that should be uploaded
     */
    public void addDocuments(Iterator<? extends DocumentContent> docs)
          throws IOException {

        PostDocumentsIterator up = new PostDocumentsIterator(this, docs, true);
            // consume the entire list to make sure everything has uploaded
        while (up.hasNext()) {
            Either<APIFailure<List<DocumentContent>>, Document> rv = up.next();
            if (rv.isLeft()) throw rv.left.exception;
        }
    }

    /**
     * Adds or updates annotations in bulk on existing documents in this
     * collection.
     *
     * @param anns List of annotations to add
     */
    public void commitAnnotations(Iterable<? extends Annotation> anns)
          throws IOException {
        commitAnnotations(anns.iterator());
    }

    /**
     * Adds or updates annotations in bulk for existing documents in this
     * collection
     *
     * The upload will terminate if an error is encountered.
     *
     * @param anns List of annotations to add
     */
    public void commitAnnotations(Iterator<? extends Annotation> anns)
          throws IOException {
        PostAnnotationsIterator up =
            new PostAnnotationsIterator(this, anns, true);

        while (up.hasNext()) {
            Either<APIFailure<List<Annotation>>, Void> rv = up.next();
            if (rv.isLeft()) throw rv.left.exception;
        }
    }

    /**
     * Returns a Task instance for the named task.
     */
    public Task task(String name) {
        return _tasks.memoize(Task.instance(this, name));
    }

    /**
     * Creates a {@link com.idibon.api.model.TaskBuilder} instance to define
     * a new {@link com.idibon.api.model.Task} in this Collection.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} on the
     * returned object to save the new task to the API.
     *
     * @param scope The type of task to create
     * @param name The new name of the new task
     * @return A {@link com.idibon.api.model.TaskBuilder} TaskBuilder to
     *         configure additional properties of the new label.
     */
    public TaskBuilder createTask(Task.Scope scope, String name) {
        return new TaskBuilder(this, scope).setName(name);
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Collection)) return false;

        Collection c = (Collection)other;
        return (c.getInterface() == getInterface() ||
                c.getInterface().equals(getInterface())) &&
            c.getEndpoint().equals(getEndpoint());
    }

    @Override public int hashCode() {
        return getEndpoint().hashCode();
    }

    /**
     * Updates stale references to a modified or deleted task across all
     * subtasks.
     */
    synchronized void commitTaskUpdate(Task task, String newName)
          throws IOException {

        boolean dirty = false;
        final String oldName = task.getName();

        JsonArray rawTasks = get(Keys.tasks);
        try {
            for (JsonObject raw : rawTasks.getValuesAs(JsonObject.class)) {
                String rawName = raw.getString(Task.Keys.name.name());
                /* the old task doesn't exist, so don't try to update it.
                 * this check could be removed if there were either A) a
                 * guarantee that no cycles exist in the ontology graph, or
                 * B) the cached collection JSON is invalidated before
                 * patching it. */
                if (rawName.equals(oldName)) continue;

                /* check if the renamed / deleted task was triggered by
                 * any of the labels in the 'raw' task. */
                JsonObject config = raw.getJsonObject(Task.Keys.config.name());
                if (config == null) continue;
                JsonObject subtasks = config.getJsonObject(CONFIG_SUBTASK_KEY);
                if (!isTriggeredSubtask(subtasks, oldName)) continue;

                /* clone the subtask hash, but replace all mentions of oldName
                 * in the arrays of triggered tasks with newName */

                Task toUpdate = task(rawName);
                JsonObjectBuilder clonedHash = JSON_BF.createObjectBuilder();

                // loop over all labels
                for (Map.Entry<String, JsonValue> entry : subtasks.entrySet()) {
                    if (!(entry.getValue() instanceof JsonArray)) continue;
                    JsonArray arr = (JsonArray)entry.getValue();
                    JsonArrayBuilder bldr = JSON_BF.createArrayBuilder();
                    // loop over all triggered tasks
                    for (JsonString json : arr.getValuesAs(JsonString.class)) {
                        /* if the triggered task matches the replaced task name,
                         * either drop it (for deleted tasks) or use newName */
                        if (json.getString().equals(oldName)) {
                            if (newName != null) bldr.add(newName);
                        } else {
                            bldr.add(json);
                        }
                    }
                    /* if the label still triggers at least one subtask, add it
                     * to the hash; otherwise, skip it. */
                    JsonArray clonedArray = bldr.build();
                    if (!clonedArray.isEmpty())
                        clonedHash.add(entry.getKey(), clonedArray);
                }

                JsonObject body = JSON_BF.createObjectBuilder()
                  .add("task", JSON_BF.createObjectBuilder()
                    .add(Task.Keys.config.name(), JSON_BF.createObjectBuilder()
                      .add(CONFIG_SUBTASK_KEY, clonedHash.build())
                    .build()).build()).build();

                Either<IOException, JsonObject> result =
                    _httpIntf.httpPost(toUpdate.getEndpoint(), body)
                    .getAs(JsonObject.class);

                if (result.isLeft()) throw result.left;

                /* since at least one task has been updated on the API,
                 * invalidate the cached API response in this Collection */
                dirty = true;
                toUpdate.invalidate().preload(result.right);
            }
        } finally {
            if (dirty) invalidate();
        }
    }

    /**
     * Checks if a named task is triggered by any label in the subtask
     * ontology hash for a task.
     *
     * @param subtasks The raw JSON hash of label names to array of task names
     * @param name The name of a task to search for
     * @return true if name is found, false if not.
     */
    @SuppressWarnings("unchecked")
    private boolean isTriggeredSubtask(JsonObject subtasks, String name) {
        if (subtasks == null) return false;

        // loop over all labels that trigger subtasks
        for (Map.Entry<String, JsonValue> entry : subtasks.entrySet()) {
            if (!(entry.getValue() instanceof JsonArray)) continue;
            JsonArray triggered = (JsonArray)entry.getValue();
            // and loop over all triggeed subtasks...
            for (JsonString json : triggered.getValuesAs(JsonString.class)) {
                if (json.getString().equals(name)) return true;
            }
        }

        // no matches, return false
        return false;
    }

    /**
     * Returns a Collection instance for the provided collection name
     *
     * @param httpIntf The HTTP interface to use to access the Collection
     * @param name The name of the collection
     */
    static Collection instance(HttpInterface httpIntf, String name) {
        return new Collection(httpIntf, name);
    }

    private Collection(HttpInterface httpIntf, String name) {
        super("/" + percentEncode(name), httpIntf);
        _name = name;
    }

    // The name of the collection (un-escaped)
    private final String _name;

    // Memoization for tasks
    private final Memoize<Task> _tasks = Memoize.cacheReferences(Task.class);
}
