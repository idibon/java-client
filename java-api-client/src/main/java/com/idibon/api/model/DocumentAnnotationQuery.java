/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.Arrays;
import javax.json.*;
import static com.idibon.api.model.Util.toJson;

/**
 * Builder for document searches by annotation properties.
 */
public class DocumentAnnotationQuery implements Cloneable {

    /**
     * Queries for documents with annotations for specific tasks.
     *
     * @param tasks List of task names
     */
    public static DocumentAnnotationQuery forTasks(String... tasks) {
        if (tasks.length == 0)
            throw new IllegalArgumentException("Task must be specified");
        DocumentAnnotationQuery result = new DocumentAnnotationQuery();
        result._taskFilter = Arrays.copyOf(tasks, tasks.length);
        return result;
    }

    /**
     * Queries for documents with annotations for specific labels.
     * <i>Note</i> When searching by label, the DocumentAnnotationQuery
     * may only search on one task.
     *
     * @param labels List of label names.
     */
    public DocumentAnnotationQuery withLabels(String... labels) {
        if (_taskFilter.length > 1 && labels.length != 0)
            throw new UnsupportedOperationException("multi-task, multi-label");
        _labelFilter = (labels.length == 0) ?
            null : Arrays.copyOf(labels, labels.length);
        return this;
    }

    /**
     * Searches for documents that have annotations created with the
     * specified provenance.
     *
     * @param provenance Annotation provenance.
     */
    public DocumentAnnotationQuery by(Annotation.Provenance provenance) {
        _provenance = provenance;
        return this;
    }

    /**
     * Searches for documents that have trainable annotations.
     */
    public DocumentAnnotationQuery andTrainable() {
        _trainableOnly = true;
        return this;
    }

    /**
     * Serializes the query to a JSON document search object.
     *
     * @param query Document search query JSON object.
     */
    DocumentAnnotationQuery serializeTo(JsonObjectBuilder query) {
        JsonArray tasks = toJson(_taskFilter);
        query.add("task", tasks);
        if (_labelFilter != null) query.add("label", toJson(_labelFilter));
        if (_provenance != null) query.add("provenance", _provenance.name());
        if (_trainableOnly) query.add("is_trainable", true);
        return this;
    }

    String[] getTasks() {
        return _taskFilter;
    }

    /**
     * Returns a deep copy of the query object.
     */
    public DocumentAnnotationQuery clone() {
        DocumentAnnotationQuery c = new DocumentAnnotationQuery();
        c._taskFilter = _taskFilter;
        c._provenance = _provenance;
        c._trainableOnly = _trainableOnly;
        c._labelFilter = _labelFilter;
        return c;
    }

    private String[] _taskFilter;
    private String[] _labelFilter;
    private Annotation.Provenance _provenance;
    private boolean _trainableOnly;
}
