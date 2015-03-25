/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.UUID;
import javax.json.*;

import com.idibon.api.util.Either;
import static com.idibon.api.model.Util.JSON_BF;

/**
 * The LabelBuilder is used to create new or update existing labels in the API.
 */
public class LabelBuilder {

    /**
     * Sets the new name for the {@link com.idibon.api.model.Label} being
     * modified or created by this LabelBuilder.
     *
     * @param name The new name for the label.
     * @return this
     */
    public LabelBuilder setName(String name) {
        if (name == null) throw new NullPointerException("name");
        if (name.isEmpty()) throw new IllegalArgumentException("name");
        _name = name;
        return this;
    }

    /**
     * Sets the new description for the {@link com.idibon.api.model.Label}
     * being modified or created by this LabelBuilder.
     *
     * @param description The new  for the label.
     * @return this
     */
    public LabelBuilder setDescription(String description) {
        if (description == null) throw new NullPointerException("description");
        _description = description;
        return this;
    }

    /**
     * Saves all changes represented by this LabelBuilder to the API, and
     * returns the resulting {@link com.idibon.api.model.Label}.
     *
     * @return The new or updated {@link com.idibon.api.model.Label}.
     */
    public Label commit() throws IOException {
        if (_existingUuid == null && _name == null)
            throw new IllegalArgumentException("New labels must have names");

        if (_name != null && _name.isEmpty())
            throw new IllegalArgumentException("Name must be non-empty");

        JsonObject task = JSON_BF.createObjectBuilder()
          .add(Task.Keys.labels.name(), JSON_BF.createArrayBuilder()
            .add(toJson()).build()).build();

        Either<IOException, JsonValue> result =
            _task.getInterface().httpPost(_task.getEndpoint(), task).get();
        if (result.isLeft()) throw result.left;

        String newName = (_name != null) ? _name : _existingLabel.getName();

        _task.commitLabelUpdate(_existingLabel, newName);
        return _task.label(newName);
    }

    /**
     * Builds the JSON payload of label details.
     */
    JsonObject toJson() {
        JsonObjectBuilder json = JSON_BF.createObjectBuilder();
        if (_existingUuid != null)
            json.add(Label.Keys.uuid.name(), _existingUuid.toString());
        if (_name != null)
            json.add(Label.Keys.name.name(), _name);
        if (_description != null)
            json.add(Label.Keys.description.name(), _description);
        return json.build();
    }

    /**
     * Constructor when new labels are being created, either by a call to
     * {@link com.idibon.api.model.Task#createLabel} or
     * {@link com.idibon.api.model.TaskBuilder#addLabel}.
     *
     * @param task The task where the new label will be created.
     */
    LabelBuilder(Task task) {
        _task = task;
        _existingLabel = null;
        _existingUuid = null;
        // The API requires new tasks to have at least an empty description
        _description = "";
    }

    /**
     * Constructor when existing labels are modified.
     *
     * @param existing The label that will be modified. The label must be
     *        committed to the API and have an API-assigned UUID.
     */
    LabelBuilder(Label existing) throws IOException {
        _task = existing.getTask();
        _existingLabel = existing;
        _existingUuid = existing.getUUID();
    }

    private final Task _task;
    private final Label _existingLabel;
    private final UUID _existingUuid;
    private String _description;
    private String _name;
}
