/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.UUID;
import java.util.Collections;
import javax.json.JsonObject;

/**
 * Class used to create new or update existing annotations.
 */
public abstract class AnnotationBuilder<T extends Annotation> {

    /**
     * Create the Annotation instance from the current builder state.
     */
    public abstract T build();

    /**
     * Builder for constructing Assignment Annotation instances
     */
    public static class Assignment
          extends AnnotationBuilder<Annotation.Assignment> {

        /**
         * Creates an AnnotationBuilder to update an existing assignment.
         *
         * @param assignment The existing annotation to update.
         * @return New AnnotationBuilder.Assignment instance.
         */
        public static AnnotationBuilder.Assignment update(Annotation.Assignment assignment) {
            return new AnnotationBuilder.Assignment(assignment);
        }

        /**
         * Creates an AnnotationBuilder to add an assignment to a new or
         * existing document.
         *
         * @param content The document to annotate (either an existing
         *        {@link com.idibon.api.model.Document} instance, or new
         *        content that will be uploaded.
         * @param label The {@link com.idibon.api.model.Label} to assign.
         *        If content is a {@link com.idibon.api.model.Document},
         *        the label must belong to the same
         *        {@link com.idibon.api.model.Collection}.
         * @return New AnnotationBuilder.Assignment instance.
         */
        public static AnnotationBuilder.Assignment on(DocumentContent content,
              Label label) {
            Task task = label.getTask();
            if (content instanceof Document) {
                Document doc = (Document)content;
                if (!doc.getCollection().equals(task.getCollection()))
                    throw new IllegalArgumentException("Mismatched collection");
            }
            return new AnnotationBuilder.Assignment(
                content, -1, -1, label);
        }

        /**
         * Creates an AnnotationBuilder to add an assignment to a span of text
         * in a new or existing document.
         *
         * @param content The document to annotate (either an existing
         *        {@link com.idibon.api.model.Document} instance, or new
         *        content that will be uploaded.
         * @param offset The start of the text span, inclusive.
         * @param length The length of the text span.
         * @param label The {@link com.idibon.api.model.Label} to assign.
         *        If content is a {@link com.idibon.api.model.Document},
         *        the label must belong to the same
         *        {@link com.idibon.api.model.Collection}.
         * @return New AnnotationBuilder.Assignment instance.
         */
        public static AnnotationBuilder.Assignment on(DocumentContent content,
              int offset, int length, Label label) {
            if (offset < 0 || length < 0)
                throw new IllegalArgumentException("invalid span");
            Task task = label.getTask();
            if (content instanceof Document) {
                Document doc = (Document)content;
                if (!doc.getCollection().equals(task.getCollection()))
                    throw new IllegalArgumentException("Mismatched collection");
            }

            return new AnnotationBuilder.Assignment(
                content, offset, length, label);
        }

        /**
         * Controls the assignment's status and is_trainable fields.
         */
        public enum Status {
            /**
             * Indicates that the assignment needs further review before it
             * can be used for machine learning (status = 'assigned',
             * is_trainable = false).
             */
            Assigned,
            /**
             * Indicates that, following review, the assignment is unsuitable
             * for use in machine learning (status = 'final',
             * is_trainable = false).
             */
            Invalid,
            /**
             * Indicates that the assignment is usable for machine learning
             * (status = 'final', is_trainable = true).
             */
            Valid,
            /**
             * Indicates that the assignment is usable for machine learning,
             * and that the item is a good test question for verifying ML
             * or annotator accuracy (status = 'Gold', is_trainable = true).
             */
            Gold;
        }

        /**
         * Generates the Assignment represented by the current builder
         * configuration.
         *
         * @return Assignment instance.
         */
        public Annotation.Assignment build() {
            if (_provenance == null)
                throw new NullPointerException("Missing provenance");

            if (_offset >= 0) {
                return new Annotation.SpanAssignment(_content, _existingUUID,
                    _active, _label, _provenance, _status, _negativeExample,
                    _trainable, _confidence, null, null, _userID, _offset,
                    _length, Collections.<JsonObject>emptyList());
            } else {
                return new Annotation.DocumentAssignment(_content,
                    _existingUUID, _active, _label, _provenance, _status,
                    _negativeExample, _trainable, _confidence, null, null,
                    _userID, Collections.<JsonObject>emptyList());
            }
        }

        /**
         * Set the confidence for this assignment. Optional.
         *
         * @param confidence A confidence value 0.0 - 1.0 defining the
         *        confidence in the correctness of the annotation. The default
         *        value for new assignments is undefined (NaN).
         * @return this
         */
        public AnnotationBuilder.Assignment confidence(double confidence) {
            _confidence = confidence;
            return this;
        }

        /**
         * Sets the provenance for this Assignment
         *
         * @param provenance The provenance for this annotation. Required.
         */
        public AnnotationBuilder.Assignment provenance(Annotation.Provenance provenance) {
            _provenance = provenance;
            return this;
        }

        /**
         * Sets the status and is_trainable flags for this Assignment.
         *
         * @param status One of the {@link com.idibon.api.model.AnnotationBuilder.Assignment.Status}
         *        values.
         * @return this.
         */
        public AnnotationBuilder.Assignment is(Status status) {
            switch (status) {
            case Assigned:
                _status = "assigned";
                _trainable = false;
                break;
            case Invalid:
                _status = "final";
                _trainable = false;
                break;
            case Valid:
                _status = "final";
                _trainable = true;
                break;
            case Gold:
                _status = "Gold";
                _trainable = true;
                break;
            }
            return this;
        }

        /**
         * Sets the user ID to the user creating the assignment. Optional.
         *
         * @param userID UUID of the user creating this assignment, or null if
         *        unknown.
         * @return this
         */
        public AnnotationBuilder.Assignment by(UUID userID) {
            _userID = userID;
            return this;
        }

        Assignment(Annotation.Assignment existingAnn) {
            _existingUUID = existingAnn.uuid;
            _content = existingAnn.document;
            _confidence = existingAnn.confidence;
            _active = existingAnn.active;
            _trainable = existingAnn.trainable;
            _negativeExample = existingAnn.negativeExample;
            _provenance = existingAnn.provenance;
            _label = existingAnn.label;
            _status = existingAnn.status;
            _userID = existingAnn.userID;
            if (existingAnn instanceof Annotation.SpanAssignment) {
                Annotation.SpanAssignment span =
                    (Annotation.SpanAssignment)existingAnn;
                _length = span.length;
                _offset = span.offset;
            } else {
                _length = -1;
                _offset = -1;
            }
        }

        Assignment(DocumentContent cont, int offset, int length, Label label) {
            _content = cont;
            _existingUUID = null;
            _confidence = Double.NaN;
            _trainable = false;
            _active = true;
            _negativeExample = false;
            _label = label;
            _offset = offset;
            _length = length;
        }

        private final DocumentContent _content;
        private final UUID _existingUUID;
        private final Label _label;
        private final int _offset;
        private final int _length;
        private boolean _active;
        private boolean _trainable;
        private Annotation.Provenance _provenance;
        private boolean _negativeExample;
        private double _confidence;
        private String _status;
        private UUID _userID;
    }

    /**
     * Builder for constructing Judgment Annotation instances.
     */
    public static class Judgment
          extends AnnotationBuilder<Annotation.Judgment> {

        /**
         * Update an existing {@link com.idibon.api.model.Annotation.Judgment}
         *
         * @param existingAnn The existing Annotation.Judgment to update.
         */
        public static AnnotationBuilder.Judgment update(Annotation.Judgment existingAnn) {
            return new AnnotationBuilder.Judgment(existingAnn);
        }

        /**
         * Add a judgment on an {@link com.idibon.api.model.Annotation.Assignment}
         *
         * @param assignment The Annotation.Assignment to judge.
         */
        public static AnnotationBuilder.Judgment on(Annotation.Assignment assignment) {
            return new AnnotationBuilder.Judgment(assignment);
        }

        /**
         * Generates the Judgment represented by the current builder
         * configuration.
         *
         * @return Judgment instance.
         */
        public Annotation.Judgment build() {
            return new Annotation.Judgment(_existingUUID, _active, _assignment,
                _disagreement, null, null, _userID);
        }

        /**
         * Sets the user ID to the user performing the judgment.
         *
         * @param userID UUID of the user performing this judgment, or null if
         *        unknown.
         * @return this
         */
        public AnnotationBuilder.Judgment by(UUID userID) {
            _userID = userID;
            return this;
        }

        /**
         * Sets the disagreement flag for this judgment.
         *
         * @param disagreement When true, the judgment disagrees with the
         *        underlying Annotation.Assignment. When false, the judgment
         *        supports the assignment.
         * @return this
         */
        public AnnotationBuilder.Judgment disagreement(boolean disagreement) {
            _disagreement = disagreement;
            return this;
        }

        /**
         * Sets the active flag for this judgment.
         *
         * @param active When false, the judgment will be ignored by internal
         *        machine learning processes. The default for newly-created
         *        judgments is true.
         * @return this
         */
        public AnnotationBuilder.Judgment active(boolean active) {
            _active = active;
            return this;
        }

        Judgment(Annotation.Judgment existingAnn) {
            _assignment = existingAnn.assignment;
            _existingUUID = existingAnn.uuid;
            _userID = existingAnn.userID;
            _disagreement = existingAnn.disagreement;
            _active = existingAnn.active;
        }

        Judgment(Annotation.Assignment assignment) {
            if (assignment.uuid == null)
                throw new IllegalStateException("Can't judge new assignment");

            _assignment = assignment;
            _existingUUID = null;
            _active = true;
        }

        private final Annotation.Assignment _assignment;
        private final UUID _existingUUID;
        private boolean _disagreement;
        private boolean _active;
        private UUID _userID;
    }
}
