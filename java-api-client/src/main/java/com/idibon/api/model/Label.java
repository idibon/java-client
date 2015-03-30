/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.*;
import javax.json.*;

import com.idibon.api.util.Either;
import com.idibon.api.model.Collection;
import static com.idibon.api.model.Util.JSON_BF;

/**
 * A label (classification / type of data) inside a Task.
 */
public class Label {

    /**
     * Keys that may be present in a Label JSON.
     */
    public enum Keys {
        /**
         * Date the label was created (<tt>ISO-8601 String</tt>).
         */
        created_at,
        /**
         * Detailed description of the element (<tt>String</tt>).
         */
        description,
        /**
         * Indicates if the Label is active (<tt>Boolean</tt>).
         */
        is_active,
        /**
         * The name of the Label (<tt>String</tt>).
         */
        name,
        /**
         * The UUID of the {@link com.idibon.api.model.Task} that own
         * this label (<tt>UUID String</tt>).
         */
        task_id,
        /**
         * Date the label was most recently updated (<tt>ISO-8601 String</tt>).
         */
        updated_at,
        /**
         * UUID for the label (<tt>ISO-8601 String</tt>).
         */
        uuid;
    }

    /**
     * Return the JSON detailed data for this label.
     *
     * @return The {@link javax.json.JsonObject} detailed data for this label.
     * @throws NoSuchElementException if the label is not found in the task.
     */
    public JsonObject getJson() throws IOException {
        JsonArray allLabels = _task.get(Task.Keys.labels);
        for (JsonObject label : allLabels.getValuesAs(JsonObject.class)) {
            if (label.getString("name").equals(_name))
                return label;
        }

        throw new NoSuchElementException("Not in task: '" + _name + "'");
    }

    /**
     * Returns the label's UUID.
     *
     * @return UUID for the label.
     * @throws NoSuchElementException if the label hasn't been committed
     * @throws IOException if an error occurs communicating over the API.
     */
    public UUID getUUID() throws IOException {
        String raw = getJson().getString(Keys.uuid.name(), null);
        return UUID.fromString(raw);
    }

    /**
     * Returns the long description for this label, if present.
     */
    public String getDescription() throws IOException {
        return getJson().getString(Keys.description.name(), null);
    }

    /**
     * Gets the name of the label.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns all of the {@link com.idibon.api.model.Task} that should be
     * triggered when this Label is confidently predicted by the API.
     *
     * @return Set of {@link com.idibon.api.model.Task} objects.
     */
    public Set<Task> getSubtasks() throws IOException {
        Map<Label, Set<? extends Task>> allSubtasks = _task.getSubtasks();
        if (allSubtasks == null) return Collections.<Task>emptySet();
        Set<? extends Task> subtasks = allSubtasks.get(this);
        if (subtasks == null) return Collections.<Task>emptySet();
        return Collections.<Task>unmodifiableSet(subtasks);
    }

    /**
     * Returns a {@link com.idibon.api.model.LabelBuilder} to modify the
     * properties of this label.
     *
     * You must call {@link com.idibon.api.model.LabelBuilder#commit} to save
     * any changes to this label back to the API.
     *
     * @return {@link com.idibon.api.model.LabelBuilder} modifying this label.
     */
    public LabelBuilder modify() throws IOException {
        return new LabelBuilder(this);
    }

    /**
     * Deletes the label.
     */
    public void delete() throws IOException {
        JsonObject body = JSON_BF.createObjectBuilder()
            .add("label", getName()).build();

        Either<IOException, JsonObject> result =
            _task.getInterface().httpDelete(_task.getEndpoint(), body)
            .getAs(JsonObject.class);

        if (result.isLeft()) throw result.left;
        if (!result.right.getBoolean("deleted"))
            throw new IOException("Label was not deleted");

        _task.commitLabelUpdate(this, null);
    }

    /**
     * Gets the Task for this Label.
     */
    public Task getTask() {
        return _task;
    }

    /**
     * Returns the rules associated with this label.
     *
     * @return All of the rules associated with this label, or an empty list.
     */
    public List<? extends TuningRules.Rule> getRules() throws IOException {
        List<? extends TuningRules.Rule> rules = _task.getRules().get(this);
        if (rules == null)
            return Collections.<TuningRules.Rule>emptyList();
        return Collections.unmodifiableList(rules);
    }

    /**
     * Creates a new tuning rule for this label from a phrase string.
     *
     * Creates a new {@link com.idibon.api.model.TuningRules.Rule} for this
     * label, automatically determining the correct type of rule (i.e.,
     * Substring or Regex) from the provided phrase. Callers must pass the
     * returned rule to {@link com.idibon.api.model.Task#addRules(TuningRules.Rule...)}
     * before the rule will take effect.
     *
     * @param phrase The prase for the new rule. Phrases wrapped in backslash
     *        (<tt>/</tt>, ASCII <tt>2F</tt>) characters will be interpreted as
     *        regular expressions, like regex literals in JavaScript and Perl.
     * @param weight The weight for the new rule. Valid weights are between 0.0
     *        and 1.0, inclusive. See {@link com.idibon.api.model.TuningRules}
     *        for the interpretation of weight values.
     * @return A rule that can be added to this label's
     *        {@link com.idibon.api.model.Task}.
     */
    public TuningRules.Rule createRule(String phrase, double weight) {
        if (weight < 0.0 || weight > 1.0)
            throw new IllegalArgumentException("Invalid value for weight");

        return TuningRules.Rule.parse(this, phrase, weight);
    }

    /**
     * Creates a new tuning rule for this label from a regular expression.
     */
    public TuningRules.Rule.Regex createRule(Pattern phrase, double weight) {
        return new TuningRules.Rule.Regex(this, "/" + phrase.toString() + "/", weight);
    }

    @Override public String toString() {
        return "Label<" + _task.getName() + "#" + _name + ">";
    }

    @Override public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Label)) return false;
        Label l = (Label)other;

        return l.getName().equals(_name) &&
            l.getTask().equals(_task);
    }

    @Override public int hashCode() {
        return _name.hashCode();
    }

    static Label instance(Task task, String name) {
        return new Label(task, name);
    }

    Label(Task task, String name) {
        _task = task;
        _name = name;
    }

    private final String _name;
    private final Task _task;
}
