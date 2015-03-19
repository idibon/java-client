/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;

import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import java.io.IOException;
import javax.json.*;

import com.idibon.api.http.HttpInterface;
import com.idibon.api.model.Collection;
import static com.idibon.api.model.Util.JSON_BF;

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

    /**
     * Returns all of the ML tuning rules defined for this task.
     */
    public Map<Label, List<TuningRules.Rule>> getRules() throws IOException {
        return Collections.unmodifiableMap(getCachedTuningRules());
    }

    /**
     * Adds new tuning rules to the task, or update the weights for existing
     * rules.
     *
     * @param rules List of rules to add. If an identical rule already exists
     *        in the task, the rules' weight will be updated to the new value.
     */
    public void addRules(TuningRules.Rule... rules) throws IOException {
        TuningRules tuning = getCachedTuningRules().clone();

        /* loop over all arguments; if the rule isn't found elsewhere in
         * the existing dictionary, add it */
        for (TuningRules.Rule rule : rules) {
            if (!rule.label.getTask().equals(this))
                throw new IllegalArgumentException("Wrong task on rule");

            List<TuningRules.Rule> labelRules = tuning.get(rule.label);
            if (labelRules == null) {
                labelRules = new ArrayList<>();
                tuning.put(rule.label, labelRules);
            }

            Iterator<TuningRules.Rule> existingRules = labelRules.iterator();
            /* delete every rule for the same phrase as the new rule
             * TODO: build a map of the new phrases as a pre-process, so this
             * is O(N+M), rather than O(NM) */
            while (existingRules.hasNext()) {
                TuningRules.Rule existing = existingRules.next();
                if (existing.phrase.equals(rule.phrase))
                    existingRules.remove();
            }
            labelRules.add(rule);
        }

        JsonObject task = JSON_BF.createObjectBuilder()
            .add("task", JSON_BF.createObjectBuilder()
                 .add("config", Util.toJson(tuning)).build()).build();

        Future<JsonValue> result = _httpIntf.httpPost(getEndpoint(), task);
        try {
            JsonValue v = result.get();
            /* if the dictionary update was successful, overwrite the cached
             * copy with the updated rules. */
            _tuningRules = tuning;
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            throw new IOException("Error posting rules", ex.getCause());
        } catch (InterruptedException ex) {}
    }

    /**
     * Deletes one or more existing tuning rules.
     *
     * @param rules Array of tuning rules to delete from this task's tuning
     *        dictionary. All of the rules must be for labels within this task.
     */
    public void deleteRules(TuningRules.Rule... rules) throws IOException {
        TuningRules tuning = getCachedTuningRules().clone();
        boolean dirty = false;

        /* loop over all arguments, deleting each from the local tuning
         * dictionary copy. */
        for (TuningRules.Rule rule : rules) {
            if (!rule.label.getTask().equals(this))
                throw new IllegalArgumentException("Wrong task on rule");

            List<TuningRules.Rule> labelRules = tuning.get(rule.label);
            if (labelRules == null) continue;
            Iterator<TuningRules.Rule> ruleIter = labelRules.iterator();

            while (ruleIter.hasNext()) {
                // Search for the first matching rule
                TuningRules.Rule existing = ruleIter.next();
                if (existing.equals(rule)) {
                    // match found, delete it and quit the loop
                    ruleIter.remove();
                    dirty = true;
                    break;
                }
            }
        }

        if (!dirty) return;  // no changes needed, don't upload

        JsonObject task = JSON_BF.createObjectBuilder()
            .add("task", JSON_BF.createObjectBuilder()
                 .add("config", Util.toJson(tuning)).build()).build();
        Future<JsonValue> result = _httpIntf.httpPost(getEndpoint(), task);
        try {
            JsonValue v = result.get();
            /* if the dictionary update was successful, overwrite the cached
             * copy with the updated rules. */
            _tuningRules = tuning;
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            throw new IOException("Error posting rules", ex.getCause());
        } catch (InterruptedException ex) {}

    }

    /**
     * Forces cached JSON data to be reloaded from the server.
     */
    @SuppressWarnings("unchecked")
    @Override public Task invalidate() {
        super.invalidate();
        _tuningRules = null;
        return this;
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

    /**
     * Private helper function to get the cached copy of the tuning rules,
     * or create and cache an instance if one does not already exist.
     */
    private TuningRules getCachedTuningRules() throws IOException {
        TuningRules tuning = _tuningRules;
        if (tuning == null) {
            tuning = TuningRules.parse(this,
                getJson().getJsonObject(Keys.config.name()));
            _tuningRules = tuning;
        }
        return tuning;
    }

    private volatile TuningRules _tuningRules;
    private final Collection _parent;
    private final String _name;
}
