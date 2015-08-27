/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;
import java.io.IOException;
import java.io.StringReader;

import javax.json.*;

import com.idibon.api.util.Either;
import com.idibon.api.util.Memoize;
import com.idibon.api.http.HttpInterface;
import com.idibon.api.model.Collection;

import static com.idibon.api.model.Util.*;
import static com.idibon.api.model.TuningRules.CONFIG_TUNING_KEY;
import static com.idibon.api.model.OntologyNode.CONFIG_SUBTASK_KEY;

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
     * Classifies a single document.
     *
     * This is the same as calling classifications(Arrays.asList(doc)),
     * accessing the first (and only) element returned from the iterable, and
     * either throwing the exception on error or returning the prediction
     * object on success.
     *
     * @param doc A {@link com.idibon.api.model.DocumentContent} to predict.
     * @return The {@link com.idibon.api.model.SpanPrediction} result.
     */
    public DocumentPrediction classifications(DocumentContent doc)
          throws IOException {
        Either<APIFailure<DocumentContent>, DocumentPrediction> result =
            classifications(Arrays.asList(doc)).iterator().next();
        if (result.isLeft()) throw result.left.exception;
        return result.right;
    }

    /**
     * Calls the prediction API to classify the list of documents according to
     * the {@link com.idibon.api.model.Label} defined for this Task.
     *
     * @param items Items to predict
     * @return A PredictionIterable that can be adjusted to return extra
     *         information for each prediction (such as significant features),
     *         and which lazily classifies all of the listed documents.
     */
    public PredictionIterable<DocumentPrediction> classifications(
          Iterable<? extends DocumentContent> items) throws IOException {
        
        PredictionIterable<DocumentPrediction> docPredictions;

        if (getScope() != Scope.document)
            throw new UnsupportedOperationException("Not a document task");
               
        if (this.isTrivialAccept()) {
            /* If this is a task with no rules defined, there's no need to make API calls for predictions.
             * Just return a stock response instead.
             */
            docPredictions = (PredictionIterable<DocumentPrediction>) new PredictionIterableTrivial<DocumentPrediction>(
                DocumentPrediction.class, this, items);
        } else {
            docPredictions = (PredictionIterable<DocumentPrediction>) new PredictionIterableNontrivial<DocumentPrediction>(
                DocumentPrediction.class, this, items);
        }
        
        return docPredictions;
    }

    /**
     * Extracts spans from a single document.
     *
     * This is the same as calling spans(Arrays.asList(doc)), accessing the
     * first (and only) element returned from the iterable, and either throwing
     * the exception on error or returning the prediction object on success.
     *
     * @param doc A {@link com.idibon.api.model.DocumentContent} to predict.
     * @return The {@link com.idibon.api.model.SpanPrediction} result.
     */
    public SpanPrediction spans(DocumentContent doc) throws IOException {
        Either<APIFailure<DocumentContent>, SpanPrediction> result =
            spans(Arrays.asList(doc)).iterator().next();
        if (result.isLeft()) throw result.left.exception;
        return result.right;
    }

    /**
     * Calls the prediction API to extract spans for a list of documents
     * against this Task.
     *
     * @param items The {@link com.idibon.api.model.DocumentContent}
     *              instances to predict.
     * @return A PredictionIterable that can be adjusted to return extra
     *         information for each prediction, and which lazily extracts
     *         spans on all of the listed documents.
     */
    public PredictionIterable<SpanPrediction> spans(
          Iterable<? extends DocumentContent> items) throws IOException {
        if (getScope() != Scope.span)
            throw new UnsupportedOperationException("Not a span task");

        return new PredictionIterableNontrivial<SpanPrediction>(
            SpanPrediction.class, this, items);
    }

    /**
     * Create a label instance for a label with the provided name in
     * the current task.
     */
    public Label label(String name) {
        return _labels.memoize(Label.instance(this, name));
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
     * Returns the task UUID
     */
    public UUID getUUID() throws IOException {
        UUID uuid = (UUID)getCache().get(Keys.uuid);

        if (uuid == null) {
            String raw = getJson().getString(Keys.uuid.name(), null);
            uuid = UUID.fromString(raw);
            getCache().put(Keys.uuid, uuid);
        }

        return uuid;
    }

    /**
     * Returns one of the Date keys, or null.
     *
     * @param dateKey The date value to retrieve
     * @return The requested date, or null.
     */
    public Date getDate(Keys dateKey) throws IOException {
        if (dateKey != Keys.updated_at && dateKey != Keys.created_at &&
              dateKey != Keys.trained_at && dateKey != Keys.touched_at) {
            throw new IllegalArgumentException("Not a date key");
        }

        Date date = (Date)getCache().get(dateKey);
        if (date == null) {
            date = parseDate(getJson().getString(dateKey.name(), null));
            if (date != null) getCache().put(dateKey, date);
        }

        return date;
    }

    /**
     * Returns the Task scope
     *
     * @return {@link com.idibon.api.model.Task.Scope} for this task.
     */
    public Scope getScope() throws IOException {
        return Scope.valueOf((this.<JsonString>get(Keys.scope)).getString());
    }

    /**
     * Returns all of the labels within this task.
     *
     * @return List of all of the {@link com.idibon.api.model.Label} objects
     *         defined for this task.
     */
    public List<? extends Label> getLabels() throws IOException {
        JsonArray labelJson = get(Keys.labels);
        List<Label> javaLabels = new ArrayList<>(labelJson.size());
        for (JsonObject obj : labelJson.getValuesAs(JsonObject.class))
            javaLabels.add(label(obj.getString(Label.Keys.name.name())));
        return javaLabels;
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
     *
     * @return An unmodifiable map of all of the tuning rules for the task.
     */
    @SuppressWarnings("unchecked")
    public Map<Label, List<? extends TuningRules.Rule>> getRules()
          throws IOException {
        /* javac incorrectly reports that casting Map<X, List<Y>> to
         * Map<X, List<? extends Y>> is inconvertible. cast through
         * Object to silence this error. */
        Map<Label, List<? extends TuningRules.Rule>> r =
            (Map<Label, List<? extends TuningRules.Rule>>)(Object)
            getCachedTuningRules();
        return Collections.unmodifiableMap(r);
    }

    /**
     * Creates a {@link com.idibon.api.model.TaskBuilder} instance to modify
     * properties of this {@link com.idibon.api.model.Task}.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} on the
     * returned object to save the modifications to the API.
     *
     * @return {@link com.idibon.api.model.TaskBuilder} modifying this Task.
     */
    public TaskBuilder modify() {
        return new TaskBuilder(this);
    }

    /**
     * Deletes this task, and removes all references to it from the ontology.
     */
    public void delete() throws IOException {
        JsonObject body = JSON_BF.createObjectBuilder()
            .add("task", true).build();

        Either<IOException, JsonObject> result =
            _httpIntf.httpDelete(getEndpoint(), body).getAs(JsonObject.class);

        if (result.isLeft()) throw result.left;
        if (!result.right.getBoolean("deleted"))
            throw new IOException ("Task was not deleted");

        // propagate the delete to all parent task ontologies
        _parent.commitTaskUpdate(this, null);
    }

    /**
     * Creates a {@link com.idibon.api.model.LabelBuilder} instance to define
     * a new {@link com.idibon.api.model.Label} for this Task.
     *
     * You must call {@link com.idibon.api.model.LabelBuilder#commit} on the
     * returned object to save the new label to the API.
     *
     * @param name The name of the new label to create. Must be unique.
     * @return A LabelBuilder to configure additional properties of the
     *         new label.
     */
    public LabelBuilder createLabel(String name) {
        return new LabelBuilder(this).setName(name);
    }

    /**
     * Adds new tuning rules to the task, or update the weights for existing
     * rules.
     *
     * Modification of tuning rules is not idempotent, so this method is
     * designed to prevent concurrent actions that could result in non-
     * deterministic results. If multiple {@link com.idibon.api.model.Task}
     * instances are created for the same API task, the application must
     * take care to ensure that all calls to {@link com.idibon.api.model.Task#addRules}
     * and {@link com.idibon.api.model.Task#deleteRules} are serialized
     * to ensure consistent results.
     *
     * @param rules List of rules to add. If an identical rule already exists
     *        in the task, the rules' weight will be updated to the new value.
     */
    public synchronized void addRules(TuningRules.Rule... rules)
          throws IOException {
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
              .add("config", JSON_BF.createObjectBuilder()
                .add(CONFIG_TUNING_KEY, Util.toJson(tuning)).build())
              .build()).build();

        /* the API returns the entire task structure on a successful update,
         * so switch to it after a successful POST */
        Either<IOException, JsonObject> result =
            _httpIntf.httpPost(getEndpoint(), task).getAs(JsonObject.class);

        if (result.isLeft()) throw result.left;
        invalidate(); // invalidate cached parse results
        preload(result.right);
    }

    /**
     * Deletes one or more existing tuning rules.
     *
     * Modification of tuning rules is not idempotent, so this method is
     * designed to prevent concurrent actions that could result in non-
     * deterministic results. If multiple {@link com.idibon.api.model.Task}
     * instances are created for the same API task, the application must
     * take care to ensure that all calls to {@link com.idibon.api.model.Task#addRules}
     * and {@link com.idibon.api.model.Task#deleteRules} are serialized
     * to ensure consistent results.
     *
     * @param rules Array of tuning rules to delete from this task's tuning
     *        dictionary. All of the rules must be for labels within this task.
     */
    public synchronized void deleteRules(TuningRules.Rule... rules)
          throws IOException {
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

            if (labelRules.isEmpty()) tuning.remove(rule.label);
        }

        if (!dirty) return;  // no changes needed, don't upload

        JsonObject task = JSON_BF.createObjectBuilder()
            .add("task", JSON_BF.createObjectBuilder()
              .add(Keys.config.name(), JSON_BF.createObjectBuilder()
                .add(CONFIG_TUNING_KEY, Util.toJson(tuning)).build())
              .build()).build();

        /* the API returns the entire task structure on a successful update,
         * so switch to it after a successful POST */
        Either<IOException, JsonObject> result =
            _httpIntf.httpPost(getEndpoint(), task).getAs(JsonObject.class);
        if (result.isLeft()) throw result.left;
        invalidate(); // clear out cached parse results
        preload(result.right);
    }

    /**
     * Returns true if child is an ontological descendent of this task.
     *
     * @param check A {@link com.idibon.api.model.Task} to test.
     * @return true if child is located within the ontology rooted at
     *         the current node. false if not.
     */
    public boolean isDescendent(Task check) throws IOException {
        // keep track of what has already been tested, to avoid infinite loops
        Set<Task> tested = new HashSet<>();
        Deque<Task> toTest = new LinkedList<Task>();
        toTest.add(this);

        /* this is a recursive breadth-first search over all of the
         * subtasks defined for this task (and the descendents of those
         * subtasks) to determine if 'check' exists anywhere in the tree. */
        while (!toTest.isEmpty()) {
            Task head = toTest.removeFirst();
            tested.add(head);
            if (check.equals(head)) return true;
            OntologyNode node = check.getCachedOntologyNode();
            for (Set<Task> descendents : node.values()) {
                for (Task descendent : descendents) {
                    if (!tested.contains(descendent))
                        toTest.add(descendent);
                }
            }
        }

        return false;
    }

    /**
     * Returns the list of sub-tasks that are triggered by predictions
     * for each {@link com.idibon.api.model.Label} in this task.
     *
     * @return Map of labels to subtasks
     */
    @SuppressWarnings("unchecked")
    public Map<Label, Set<? extends Task>> getSubtasks() throws IOException {
        return (Map<Label, Set<? extends Task>>)(Object)
            Collections.unmodifiableMap(getCachedOntologyNode().clone());
    }

    /**
     * Configures a subtask trigger: when a document is classified as the
     * provided {@link com.idibon.api.model.Label}, the document should also
     * be classified against the triggered {@link com.idibon.api.model.Task}.
     *
     * @param trigger The {@link com.idibon.api.model.Label} in this task that
     *        will trigger the subtasks.
     * @param tasks The new {@link com.idibon.api.model.Task} instances that
     *        should be triggered by label.
     */
    public synchronized void addSubtaskTriggers(Label trigger, Task... tasks)
          throws IOException {
        OntologyNode node = getCachedOntologyNode().clone();
        Set<Task> triggered = node.getOrDefault(trigger);

        for (Task t : tasks) {
            if (t.isDescendent(this))
                throw new IllegalArgumentException("Cyclic Ontology");

            triggered.add(t);
        }

        JsonObject task = JSON_BF.createObjectBuilder()
            .add("task", JSON_BF.createObjectBuilder()
              .add("config", JSON_BF.createObjectBuilder()
                .add(CONFIG_SUBTASK_KEY, Util.toJson(node)).build())
              .build()).build();

        Either<IOException, JsonObject> result =
            _httpIntf.httpPost(getEndpoint(), task).getAs(JsonObject.class);
        if (result.isLeft()) throw result.left;
        invalidate(); // clear out cached parse results
        _parent.invalidate();
        preload(result.right);
    }

    /**
     * Deletes a subtask trigger.
     *
     * @param trigger The {@link com.idibon.api.model.Label} in this task that
     *        triggers the subtasks.
     * @param tasks The new {@link com.idibon.api.model.Task} instances that
     *        should not be triggered by label.
     */
    public synchronized void deleteSubtaskTriggers(Label trigger, Task... tasks)
          throws IOException {
        OntologyNode node = getCachedOntologyNode().clone();
        Set<Task> triggered = node.get(trigger);
        if (triggered == null) return;

        for (Task t : tasks) triggered.remove(t);

        if (triggered.isEmpty()) node.remove(trigger);

        JsonObject task = JSON_BF.createObjectBuilder()
            .add("task", JSON_BF.createObjectBuilder()
              .add("config", JSON_BF.createObjectBuilder()
                .add(CONFIG_SUBTASK_KEY, Util.toJson(node)).build())
              .build()).build();

        Either<IOException, JsonObject> result =
            _httpIntf.httpPost(getEndpoint(), task).getAs(JsonObject.class);
        if (result.isLeft()) throw result.left;
        invalidate(); // clear out cached parsed results
        _parent.invalidate();
        preload(result.right);
    }

    /**
     * Forces cached JSON data to be reloaded from the server.
     */
    @SuppressWarnings("unchecked")
    @Override public Task invalidate() {
        super.invalidate();
        _tuningRules = null;
        _ontology = null;
        return this;
    }

    @Override public String toString() {
        return "Task<" + _parent.getName() + "#" + _name + ">";
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

    /**
     * Commits a list of created or modified labels to the API.
     *
     * @param changes All of the new labels to create or existing labels to
     *        update.
     * @return A list of all of the affected labels.
     */
    synchronized void commitLabelUpdate(Label existing,
          String name) throws IOException {

        if (existing != null && !existing.getName().equals(name)) {
            updateLabelInConfig(existing.getName(), name);
            _labels.remove(existing);
        } else {
            invalidate();
        }
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
        _labels = Memoize.cacheReferences(Label.class);
    }

    /**
     * Private helper function to update all references to a renamed or
     * deleted label in the ontology / rules / kit configurations, which
     * rely on the string name for linkage rather than the UUID.
     *
     * @param oldName The (previous) name of the label that was changed
     * @param newName The new name for the label, or null if the label
     *        was deleted.
     */
    private void updateLabelInConfig(String oldName, String newName)
          throws IOException {
        JsonObject config = get(Keys.config);
        if (config == null) return;

        JsonObjectBuilder updates = null; // lazily created when needed

        /* the tuning dictionary and ontology are both hashes of label name
         * to opaque per-label data. both can be implemented by shallow-copying
         * the hash, and renaming / skipping the key of the renamed label. */
        for (String key : new String[]{CONFIG_TUNING_KEY, CONFIG_SUBTASK_KEY}) {
            JsonObject old = config.getJsonObject(key);
            if (old != null && old.get(oldName) != null) {
                JsonObjectBuilder cloned = JSON_BF.createObjectBuilder();
                for (Map.Entry<String, JsonValue> entry : old.entrySet()) {
                    String target = entry.getKey();
                    if (target.equals(oldName)) target = newName;
                    if (target != null) cloned.add(target, entry.getValue());
                }
                if (updates == null) updates = JSON_BF.createObjectBuilder();
                updates.add(key, cloned.build());
            }
        }

        if (updates == null) {
            invalidate();
            return;
        }

        JsonObject task = JSON_BF.createObjectBuilder()
          .add("task", JSON_BF.createObjectBuilder()
             .add(Keys.config.name(), updates.build()).build()).build();

        Either<IOException, JsonObject> result =
            _httpIntf.httpPost(getEndpoint(), task).getAs(JsonObject.class);
        if (result.isLeft()) throw result.left;
        invalidate(); // clear out cached parse results
        preload(result.right);
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

    private OntologyNode getCachedOntologyNode() throws IOException {
        OntologyNode node = _ontology;
        if (node == null) {
            node = OntologyNode.parse(this,
                getJson().getJsonObject(Keys.config.name()));
            _ontology = node;
        }
        return node;
    }
    
    /**
     * Private helper function to inspect the task and determine whether it qualifies
     * as a trivial-accept case. Criteria include:
     *  1. No tuning dictionary entries
     *  2. A single feature 'ClarabridgeRule' consisting of empty arrays.
     */
    private boolean isTrivialAccept() throws IOException {    
        JsonArray features = this.getJson().getJsonArray("features");
        
        // If there are dictionary tuning rules, this is not trivial
        Map<Label, List<? extends TuningRules.Rule>> rules = this.getRules();
        if (rules.size() > 0) {
            for (List<?> rule : rules.values()) {
                if (rule.size() > 0)
                    return false;
            }
        }

        // If there is not exactly one feature named 'ClarabridgeRule', this is not trivial
        if (features.size() != 1)
            return false;
        JsonObject feature = features.getJsonObject(0);
        if (!feature.getString("name").equals(TRIVIAL_ACCEPT_FEATURE_NAME))
            return false;
        
        /* If the ClarabridgeRule is not full of empty arrays, this is not trivial.
         * Annoyingly, the label_rules portion is not a JsonObject; rather, it is a string that wants to be
         * a JsonObject, complete with escape characters for each quotation mark. Therefore, we have to do a bit
         * of cleanup before we can turn it into something we can parse: a JsonObject.
         */
        JsonObject parameters = feature.getJsonObject("parameters");
        String labelRulesString = parameters.getJsonString("label_rules").getString();
        
        // Interpret the string to turn it into a real boy
        JsonObject labelRules = Json.createReader(new StringReader(labelRulesString)).readObject();
 
        Set<String> lrulesKeys = labelRules.keySet();
        for (String lrulesKey : lrulesKeys) {
            JsonArray tmpArray = labelRules.getJsonArray(lrulesKey);
            List<JsonArray> tmpArrayValues = tmpArray.getValuesAs(JsonArray.class);            
            for (JsonArray tmpArray2 : tmpArrayValues) {
                for (int i = 0; i < tmpArray2.size(); i++) {
                    // If the contents are not empty, it is not trivial                    
                    if(tmpArray2.getString(i).length() > 0)
                        return false;
                }
            }
        }
        
        return true;
    }

    private static final String TRIVIAL_ACCEPT_FEATURE_NAME = "ClarabridgeRule";
    private volatile TuningRules _tuningRules;
    private volatile OntologyNode _ontology;
    private final Memoize<Label> _labels;
    private final Collection _parent;
    private final String _name;
}
