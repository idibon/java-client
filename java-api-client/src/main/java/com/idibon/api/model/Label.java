/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.List;
import javax.json.*;

/**
 * A label (classification / type of data) inside a Task.
 */
public class Label {

    /**
     * Gets the name of the label.
     */
    public String getName() {
        return _name;
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
     * @returns All of the rules associated with this label, or an empty list.
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
