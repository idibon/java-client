/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
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
