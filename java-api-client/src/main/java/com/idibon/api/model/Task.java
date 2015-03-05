/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import javax.json.*;

import com.idibon.api.http.HttpInterface;

/**
 * A machine learning task inside a Collection.
 *
 * Tasks contain Labels that represent specific extraction or classification
 * operations, and Features that convert document content and metadata into
 * machine learning features.
 */
public class Task extends IdibonHash {

    /**
     * Keys that may be present in a Task JSON.
     */
    public enum Keys {
        /**
         * UUID of the Collection that owns this Task (<tt>UUID String</tt>).
         */
        collection_id,
        /**
         * Extra configuration data, such as rules, for this task
         * (<tt>JsonObject</tt>).
         */
        config,
        /**
         * Date the Task was created (<tt>ISO-8601 String</tt>).
         */
        created_at,
        /**
         * Human-readable description about this task (<tt>String</tt>).
         */
        description,
        /**
         * Machine learning features used by this task (<tt>JsonArray</tt>)
         */
        features,
        /**
         * Indicates if the Task is active; inactive Tasks are ignored by
         * all system processes (<tt>Boolean</tt>).
         */
        is_active,
        /**
         * Labels defined for this task (<tt>JsonArray</tt>).
         */
        labels,
        /**
         * Name of the task (<tt>String</tt>).
         */
        name,
        /**
         * Task scope (i.e., type of predictive analytics performed);
         * see {@link com.idibon.api.model.Task.Scope} (<tt>String</tt>).
         */
        scope,
        /**
         * Date the task was last updated by a human (<tt>ISO-8601 String</tt>).
         */
        touched_at,
        /**
         * Indicates that the task is trainable; non-trainable tasks are
         * skipped by the system training agent (<tt>Boolean</tt>).
         */
        trainable,
        /**
         * Date the task was last trained; null if the task has never been
         * trained (<tt>ISO-8601 String</tt>).
         */
        trained_at,
        /**
         * Date the task was last updated by a human or system process
         * (<tt>ISO-8601 String</tt>).
         */
        updated_at,
        /**
         * Task UUID (<tt>UUID String</tt>).
         */
        uuid
    }

    /**
     * All tasks have a scope, which defines the type of predictive analytics
     * that the task will be annotated and trained to perform.
     */
    public enum Scope {
        /**
         * Document tasks are used to classify an entire document (content and
         * metadata features) into one or more labels.
         */
        document,
        /**
         * Span tasks are used to extract specific entity labels (e.g., NER)
         * from document content.
         */
        span
    }

    /**
     * Calls the prediction API for all of the provided documents for this
     * class.
     *
     * @param items The items that should be predicted
     * @param predictClass The class of prediction results expected, e.g.,
     *        DocumentPrediction.class for document-scope tasks,
     *        SpanPrediction.class for span-scope tasks.
     */
    public <T extends Prediction> PredictionIterable<T> predictions(
            Iterable<? extends DocumentContent> items, Class<T> predictClass) {
        return new PredictionIterable<T>(predictClass, this, items);
    }

    /**
     * Calls the prediction API to get document classification results for
     * this task. This methos is the same as calling
     * Task#predictions(items, DocumentPrediction.class)
     *
     * @param items Items to predict
     */
    public PredictionIterable<DocumentPrediction> classifications(
            Iterable<? extends DocumentContent> items) {
        return new PredictionIterable<DocumentPrediction>(
          DocumentPrediction.class, this, items);
    }

    /**
     * Create a label instance for a label with the provided name in
     * the current task.
     */
    public Label label(String name) {
        return Label.instance(this, name);
    }

    /**
     * Returns the task JSON
     */
    public JsonObject getJson() throws IOException {
        return super.getJson(null).getJsonObject("task");
    }

    /**
     * Returns the value at a key in the JSON hash.
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonValue> T get(Keys key) throws IOException {
        return (T)getJson().get(key.name());
    }

    /**
     * Returns the Task scope
     */
    public Scope getScope() throws IOException {
        return Scope.valueOf((this.<JsonString>get(Keys.scope)).getString());
    }

    /**
     * Returns the Task name
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the Collection that this task is a member of
     */
    public Collection getCollection() {
        return _parent;
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Task)) return false;

        Task t = (Task)other;
        return t.getName().equals(_name) &&
            t.getCollection().equals(_parent);
    }

    @Override public int hashCode() {
        return _name.hashCode();
    }

    static Task instance(Collection parent, String name) {
        return new Task(name, parent, parent.getInterface());
    }

     static Task instance(Collection parent, JsonObject obj) {
        String name = obj.getJsonObject("task").getString("name");
        return instance(parent, name).preload(obj);
    }

    private Task(String name, Collection parent, HttpInterface httpIntf) {
        super(parent.getEndpoint() + "/" + percentEncode(name), httpIntf);
        _name = name;
        _parent = parent;
    }

    private final Collection _parent;
    private final String _name;
}
