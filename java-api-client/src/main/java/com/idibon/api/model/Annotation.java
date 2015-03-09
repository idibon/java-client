/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;

import static com.idibon.api.model.Util.*;

/**
 * An Annotation represents an annotation on a Document.
 *
 * An annotation may be either an Assignment, or a Judgment.
 *
 *  - An Assignment defines that a Document, or region of text within
 *    a Document (i.e., a Span) is or is not an example of a specific
 *    Label.
 *
 *  - A Judgment is an independent evaluation of the correctness of
 *    an Assignment.
 */
public abstract class Annotation {

    /**
     * Keys in the annotation JSON hash.
     */
    public enum Keys {
        /**
         * <i>Reserved.</i>
         */
        boost,
        /**
         * Confidence annotation is correct (<tt>Double</tt>).
         */
        confidence,
        /**
         * Date annotation was created (<tt>ISO-8601 String</tt>).
         */
        created_at,
        /**
         * <i>Reserved.</i>
         */
        importance,
        /**
         * Indicates if the annotation is active; inactive annotations
         * are ignored by all system processes (<tt>Boolean</tt>).
         */
        is_active,
        /**
         * <i>Reserved.</i>
         */
        is_in_agreement,
        /**
         * Negation flag for the annotation; when true
         * <br />
         * <b>for assignments</b>, indicates that the annotation is a negative
         *     example (i.e., known to not be an example of the Label)
         * <br />
         * <b>for judgments</b>, indicates that the judgment rejects the
         *     original assignment, referenced by the <tt>subject_id</tt>
         * </br>
         * (<tt>Boolean</tt>).
         */
        is_negated,
        /**
         * Indicates the annotation has reached a confidence / judgment
         * threshold sufficient to be used for training (<tt>Boolean</tt>).
         */
        is_trainable,
        /**
         * <i>Assignments-only</i> The label that is applied by this
         * annotation (<tt>JsonObject</tt> with a single <tt>name</tt> key).
         */
        label,
        /**
         *  <i>Span annotations only</i> Length of the span annotation,
         * in characters (<tt>Long</tt>).
         */
        length,
        /**
         * <i>Reserved.</i>
         */
        length2,
        /**
         * <i>Span annotations only</i> Starting position in characters
         * of the span within the document content (<tt>Long</tt>).
         */
        offset,
        /**
         * <i>Reserved.</i>
         */
        offset2,
        /**
         * <i>Reserved.</i>
         */
        pending_at,
        /**
         * The provenance (creator class) of this annotation (<tt>String</tt>,
         * but must be one of the provenance enumerant values
         * {@link com.idibon.api.model.Annotation.Provenance}).
         */
        provenance,
        /**
         * <i>Reserved.<i>
         */
        queued_at,
        /**
         * <i>Reserved.</i>
         */
        requested_for,
        /**
         * The current status of this annotation; updated by the system as-
         * needed based on machine learning optimizations (<tt>String</tt>).
         */
        status,
        /**
         * <i>Judgments-only</i> The UUID of the assignment which this
         * annotation is judging (<tt>UUID String</tt>).
         */
        subject_id,
        /**
         * <i>Assignments-only</i> The task annotated by this annotation
         * (<tt>JsonObject</tt> with a single <tt>name</tt> key).
         */
        task,
        /**
         * <i>Span annotations-only</i> The text inside the span
         * (<tt>String</tt>).
         */
        text,
        /**
         * Free-form text about this annotation; often used to store a human-
         * readable reason why the annotation was created (<tt>String</tt>,
         * max 255 characters.
         */
        text2,
        /**
         * Date the annotation was most recently updated
         * (<tt>ISO-8601 String</tt>).
         */
        updated_at,
        /**
         * The UUID of the user who created this annotation, if the annotation
         * was created by a known user (<tt>UUID String</tt>).
         */
        user_id,
        /**
         * This annotation's UUID (<tt>UUID String</tt>).
         */
        uuid;
    }

    /**
     * The annotation provenance describes how the annotation was created;
     * either as a process of direct human interaction, indirectly from
     * multiple human annotations, or automatically by a predictive agent.
     * It is important to use the correct provenance when creating
     * annotations to ensure optimal ML results.
     */
    public enum Provenance {
        /**
         * Annotations created by a machine learning optimization process.
         */
        aggregation,
        /**
         * Annotations created by an initial bootstrapping process, such as
         * predictions against an external model or string matches.
         */
        bootstrapped,
        /**
         * Annotations created by an unsupervised learning process, such
         * as topic modeling.
         */
        cluster,
        /**
         * Cached predictions.
         */
        prediction,
        /**
         * Annotations created by a trusted human.
         */
        Human,
        /**
         * Annotations created by a crowd-sourced worker.
         */
        Crowd,
        /**
         * Annotations imported from a Crowdflower annotation job.
         */
        Crowdflower;
    };

    /**
     * Annotation UUID (may be null if the annotation has not been uploaded
     * to the API)
     */
    public final UUID uuid;

    /**
     * True if the annotation is active. Inactive annotations are ignored
     * for machine learning purposes, and serve only as a record of an
     * earlier annotation.
     */
    public final boolean active;

    /**
     * Date annotation was created
     */
    public final Date createdAt;

    /**
     * Date annotation was most recently modified
     */
    public final Date updatedAt;

    /**
     * User ID (may be null) of the user who created the annotation.
     */
    public final UUID userID;

    /**
     * An Assignment annotation. Subclassed to DocumentAssignment and
     * SpanAssignment.
     */
    public abstract static class Assignment extends Annotation {

        /**
         * Document that the Assignment applies to.
         */
        public final Document document;

        /**
         * Task name that is annotated by this Assignment.
         */
        public final String taskName;

        /**
         * Label name that is annotated by this Assignment.
         */
        public final String labelName;

        /**
         * Annotation provenance, defines the source of the Assignment.
         */
        public final Provenance provenance;

        /**
         * Annotation status, controls internal annotation workflow processes.
         */
        public final String status;

        /**
         * When true, the Assignment is a negative example (i.e., this Document
         * or Span is NOT a Label), when false the Assignment is a positive
         * example.
         */
        public final boolean negativeExample;

        /**
         * Controls use of the Assignment for machine learning processes.
         */
        public final boolean trainable;

        /**
         * Confidence for the annotation. If confidence is NaN, no confidence
         * value has been assigned.
         */
        public final double confidence;

        /**
         * List of judgments performed on this Annotation.
         */
        public final List<? extends Judgment> judgments;

        /**
         * Reads an Assignment from a JSON payload
         *
         * @param doc The Document that the Assignment is annotating
         * @param body The JSON annotation payload
         * @param judgments Any JSON bodies for Judgment annotations that
         *                  have been made on this Assignment.
         *
         * @return The Assignment instance.
         */
        public static Assignment parse(Document doc, JsonObject body,
              List<JsonObject> judgments) {
            UUID uuid = UUID.fromString(body.getString(Keys.uuid.name()));
            Date created = getCreatedDate(body);
            Date updated = getUpdatedDate(body);
            UUID userID = getUserID(body);

            boolean active = true;
            JsonValue activeJson = body.get(Keys.is_active.name());
            if (activeJson instanceof JsonString)
                active = Boolean.valueOf(((JsonString)activeJson).getString());
            else if (activeJson == JsonValue.FALSE)
                active = false;

            boolean negated = body.getBoolean(Keys.is_negated.name());
            boolean trainable = body.getBoolean(Keys.is_trainable.name(), false);

            JsonNumber confJson = body.getJsonNumber(Keys.confidence.name());
            double conf = confJson == null ? Double.NaN : confJson.doubleValue();

            String label = body.getJsonObject(Keys.label.name()).getString("name");
            String task = body.getJsonObject(Keys.task.name()).getString("task");

            Provenance provenance;

            try {
                String provString = body.getString(Keys.provenance.name(), "");
                provenance = Provenance.valueOf(provString);
            } catch (IllegalArgumentException _) {
                // ignore, default back to Crowd.
                provenance = Provenance.Crowd;
            }

            String status = body.getString(Keys.status.name(), "");

            JsonNumber offset = body.getJsonNumber(Keys.offset.name());
            JsonNumber length = body.getJsonNumber(Keys.length.name());

            if (offset != null) {
                return new SpanAssignment(doc, uuid, active, task, label,
                    provenance, status, negated, trainable, conf, created,
                    updated, userID, offset.intValue(), length.intValue(),
                    judgments);
            } else {
                return new DocumentAssignment(doc, uuid, active, task, label,
                    provenance, status, negated, trainable, conf, created,
                    updated, userID, judgments);
            }
        }

        Assignment(Document doc, UUID uuid, boolean active, String taskName,
              String labelName, Provenance provenance, String status,
              boolean negativeExample, boolean trainable, double confidence,
              Date createdAt, Date updatedAt, UUID userID,
              List<JsonObject> judgments) {
            super(uuid, active, createdAt, updatedAt, userID);
            this.document = doc;
            this.taskName = taskName;
            this.labelName = labelName;
            this.provenance = provenance;
            this.status = status;
            this.negativeExample = negativeExample;
            this.trainable = trainable;
            this.confidence = confidence;
            if (judgments == null) judgments = Collections.emptyList();
            List<Judgment> judge = new ArrayList<>(judgments.size());
            for (JsonObject judgment : judgments)
                judge.add(Judgment.parse(this, judgment));
            this.judgments = Collections.unmodifiableList(judge);
        }
    }

    /**
     * Simple marker subclass for Assignments that classify an entire Document.
     */
    public static class DocumentAssignment extends Assignment {
        DocumentAssignment(Document doc, UUID uuid, boolean active,
              String taskName, String labelName, Provenance provenance,
              String status, boolean negativeExample, boolean trainable,
              double confidence, Date createdAt, Date updatedAt, UUID userID,
              List<JsonObject> judgments) {
            super(doc, uuid, active, taskName, labelName, provenance, status,
                  negativeExample, trainable, confidence, createdAt,
                  updatedAt, userID, judgments);
        }
    }

    /**
     * Classfies a region of text (a span) within a Document.
     */
    public static class SpanAssignment extends Assignment {
        /**
         * Character position offset in the text where the span begins.
         */
        public final int offset;

        /**
         * Length of the span, in characters.
         */
        public final int length;

        /**
         * Returns the text that is included in the span.
         */
        public String getText() throws IOException {
            String content = this.document.getContent();
            return content.substring(this.offset, this.offset + this.length);
        }

        SpanAssignment(Document doc, UUID uuid, boolean active, String taskName,
              String labelName, Provenance provenance, String status,
              boolean negativeExample, boolean trainable, double confidence,
              Date createdAt, Date updatedAt, UUID userID, int offset,
              int length, List<JsonObject> judgments) {
            super(doc, uuid, active, taskName, labelName, provenance, status,
                  negativeExample, trainable, confidence, createdAt,
                  updatedAt, userID, judgments);
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * A Judgment annotation.
     */
    public static class Judgment extends Annotation {
        /**
         * The Assignment that is judged by this Judgment.
         */
        public final Assignment assignment;

        /**
         * When true, the Judgment disagrees with the assignment.
         */
        public final boolean disagreement;

        /**
         * Creates a new immutable Judgment instance
         */
        public static Judgment parse(Assignment assignment, JsonObject body) {
            UUID uuid = UUID.fromString(body.getString(Keys.uuid.name()));
            Date created = getCreatedDate(body);
            Date updated = getUpdatedDate(body);
            UUID userID = getUserID(body);

            boolean active = true;
            JsonValue activeJson = body.get(Keys.is_active.name());
            if (activeJson instanceof JsonString)
                active = Boolean.valueOf(((JsonString)activeJson).getString());
            else if (activeJson == JsonValue.FALSE)
                active = false;

            boolean negated = body.getBoolean(Keys.is_negated.name());

            return new Judgment(uuid, active, assignment, negated,
                                created, updated, userID);
        }

        Judgment(UUID uuid, boolean active, Assignment assignment,
              boolean disagreement, Date createdAt,
              Date updatedAt, UUID userID) {
            super(uuid, active, createdAt, updatedAt, userID);
            this.assignment = assignment;
            this.disagreement = disagreement;
        }
    }

    /**
     * Returns the UUID of the user who created the annotation, if it exists.
     *
     * @param ann Annotation JSON object
     * @return UUID of the user who created the annotation, or null.
     */
    private static UUID getUserID(JsonObject ann) {
        String userID = ann.getString(Keys.user_id.name(), null);
        return userID != null ? UUID.fromString(userID) : null;
    }

    /**
     * Returns the date and time that the annotation was created.
     *
     * @param ann Annotation JSON object
     * @return Date the annotation was created, or null
     */
    private static Date getCreatedDate(JsonObject ann) {
        JsonString date = ann.getJsonString(Keys.created_at.name());
        if (date == null)
            date = ann.getJsonString(Keys.updated_at.name());

        return (date != null) ? parseDate(date.getString()) : null;
    }

    /**
     * Returns the date and time that the annotation was most recently updated.
     *
     * @param ann Annotation JSON object
     * @return Date the annotation was last updated, or null
     */
    private static Date getUpdatedDate(JsonObject ann) {
        JsonString date = ann.getJsonString(Keys.updated_at.name());
        if (date == null)
            date = ann.getJsonString(Keys.created_at.name());

        return (date != null) ? parseDate(date.getString()) : null;
    }

    protected Annotation(UUID uuid, boolean active,
          Date createdAt, Date updatedAt, UUID userID) {
        this.uuid = uuid;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.userID = userID;
    }
}
