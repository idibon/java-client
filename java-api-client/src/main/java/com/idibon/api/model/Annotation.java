/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

/**
 * An Annotation represents an annotation on a Document.
 */
public class Annotation {

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
}
