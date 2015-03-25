/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import javax.json.*;

import com.idibon.api.util.Either;
import static com.idibon.api.model.Util.JSON_BF;

/**
 * The TaskBuilder is used to create new or update existing tasks in the API.
 *
 * TaskBuilder instances are returned by task-mutation methods, such as
 * {@link com.idibon.api.model.Collection#createTask} and
 * {@link com.idibon.api.model.Task#modify}.
 */
public class TaskBuilder {

    /**
     * Changes the name for the {@link com.idibon.api.model.Task} being
     * modified or created by this TaskBuilder.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} for the
     * changes to take effect.
     *
     * @param name The new name for the task.
     * @return this.
     */
    public TaskBuilder setName(String name) {
        if (name == null) throw new NullPointerException("name");
        if (name.isEmpty()) throw new IllegalArgumentException("name");
        _name = name;
        return this;
    }

    /**
     * Changes the description for the {@link com.idibon.api.model.Task} being
     * modified or created by this TaskBuilder.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} for the
     * changes to take effect.
     *
     * @param name The new name for the task.
     * @return this.
     */
    public TaskBuilder setDescription(String description) {
        if (description == null) throw new NullPointerException("description");
        _description = description;
        return this;
    }

    /**
     * Enables this {@link com.idibion.api.model.Task}.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} for the
     * changes to take effect.
     *
     * @return this
     */
    public TaskBuilder enable() {
        _active = Boolean.TRUE;
        return this;
    }

    /**
     * Disables this {@link com.idibion.api.model.Task}.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} for the
     * changes to take effect.
     *
     * @return this
     */
    public TaskBuilder disable() {
        _active = Boolean.FALSE;
        return this;
    }

    /**
     * Enables the <tt>trainable</tt> flag for this
     * {@link com.idibon.api.model.Task}. This is the default behavior for new
     * tasks.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} for the
     * changes to take effect.
     *
     * @return this
     */
    public TaskBuilder allowTraining() {
        _trainable = Boolean.TRUE;
        return this;
    }

    /**
     * Disables the <tt>trainable</tt> flag for this task.
     *
     * You must call {@link com.idibon.api.model.TaskBuilder#commit} for the
     * changes to take effect.
     *
     * @return this
     */
    public TaskBuilder disallowTraining() {
        _trainable = Boolean.FALSE;
        return this;
    }

    /**
     * Adds a new label to the task being created / modified.
     *
     * Same as calling <tt>addLabel(name, "")</tt>.
     *
     * @param name Name for the new label.
     * @return this
     */
    public TaskBuilder addLabel(String name) {
        return addLabel(name, "");
    }

    /**
     * Adds a new label to the task being created / modified.
     *
     * @param name Name for the new label.
     * @param description Description for the new label.
     * @return this
     */
    public TaskBuilder addLabel(String name, String description) {
        _labels.add(new LabelBuilder((Task)null).setName(name)
                    .setDescription(description));
        return this;
    }

    /**
     * Commits all of the changes represented by this TaskBuilder to the
     * target task, creating or updating as needed.
     *
     * @return The new or modified task. If the task is renamed, all existing
     *         references to the task will become invalid.
     */
    public Task commit() throws IOException {
        JsonObjectBuilder json = JSON_BF.createObjectBuilder();
        if (_name != null)
            json.add(Task.Keys.name.name(), _name);
        if (_trainable != null)
            json.add(Task.Keys.trainable.name(), _trainable);
        if (_active != null)
            json.add(Task.Keys.is_active.name(), _active);
        if (_description != null)
            json.add(Task.Keys.description.name(), _description);
        if (_scope != null)
            json.add(Task.Keys.scope.name(), _scope.name());

        if (!_labels.isEmpty()) {
            JsonArrayBuilder labelJson = JSON_BF.createArrayBuilder();
            for (LabelBuilder l : _labels) labelJson.add(l.toJson());
            json.add(Task.Keys.labels.name(), labelJson.build());
        }

        JsonObject body = JSON_BF.createObjectBuilder()
            .add("task", json.build()).build();

        Either<IOException, JsonObject> result;

        if (isNewTask()) {
            // Create a task endpoint without a task instance
            String endpoint = _collection.getEndpoint() + "/" +
                IdibonHash.percentEncode(_name);
            result = _collection.getInterface()
                .httpPut(endpoint, body)
                .getAs(JsonObject.class);
        } else {
            // just update the existing task
            result = _collection.getInterface()
                .httpPost(_task.getEndpoint(), body)
                .getAs(JsonObject.class);
        }

        if (result.isLeft()) throw result.left;

        if (isNewTask()) {
            _collection.invalidate();
            return _collection.task(_name).invalidate().preload(result.right);
        } else if (_name != null) {
            // if the task was renamed, update all stale references to it
            _collection.commitTaskUpdate(_task, _name);
            // and return a reference to the new task
            return _collection.task(_name);
        } else {
            // just refresh the task using the returned API result
            return _task.invalidate().preload(result.right);
        }
    }

    /**
     * Returns true if this label builder is constructing a new task.
     */
    private boolean isNewTask() {
        return _task == null;
    }

    TaskBuilder(Collection collection, Task.Scope scope) {
        _collection = collection;
        _task = null;
        _scope = scope;
        _active = Boolean.TRUE;
        _trainable = Boolean.TRUE;
        // The API requires new tasks to have at least an empty description
        _description = "";
    }

    TaskBuilder(Task existing) {
        _collection = existing.getCollection();
        _task = existing;
        _scope = null;
    }

    // List of labels being added
    private final List<LabelBuilder> _labels = new ArrayList<>();

    // The collection being modified
    private final Collection _collection;

    // The scope for the new task (null if the task already exists)
    private final Task.Scope _scope;

    // The existing task being modified (null if the task is new)
    private final Task _task;

    // The description (null if not changing)
    private String _description;

    // The task's trainable flag (null if not changing)
    private Boolean _trainable;

    // The task's active flag (null if not changing)
    private Boolean _active;

    // The task name (null if not changing)
    private String _name;
}
