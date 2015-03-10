/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;

import java.util.*;
import javax.json.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

final class Util {

    /**
     * JsonValue.NULL-safe getter for JSON numbers.
     *
     * @param object The JSON object to read
     * @param key The key to read.
     * @return The JsonNumber at key
     */
    static JsonNumber getJsonNumber(JsonObject object, String key) {
        JsonValue value = object.get(key);
        if (value instanceof JsonNumber) {
            return (JsonNumber)value;
        } else if (value == null || value == JsonValue.NULL) {
            return null;
        } else {
            throw new ClassCastException("Cannot cast " +
                value.getClass().getName() +
                " to " + JsonNumber.class.getName());
        }
    }

    /**
     * Creates a JsonArray from an array of Java Strings.
     *
     * @param strings Array of strings
     * @returns JsonArray
     */
    static JsonArray toJson(String[] strings) {
        JsonArrayBuilder b = JSON_BF.createArrayBuilder();
        for (String s : strings) {
            if (s == null) b.addNull();
            else b.add(s);
        }
        return b.build();
    }

    /**
     * Creates a JsonArray from an iterable collection of strings.
     *
     * @param strings
     * @returns JsonArray
     */
    static JsonArray toJson(Iterable<String> strings) {
        JsonArrayBuilder b = JSON_BF.createArrayBuilder();
        for (String s : strings) {
            if (s == null) b.addNull();
            else b.add(s);
        }
        return b.build();
    }

    /**
     * Creates a JsonObject from DocumentContent
     *
     * @param doc The document content that should be uploaded.
     * @return JSON payload to send to the server.
     */
    static JsonObject toJson(DocumentContent doc) throws IOException {
        JsonObjectBuilder bldr = JSON_BF.createObjectBuilder();

        if (doc instanceof DocumentContent.Named) {
            bldr.add(Document.Keys.name.name(),
                     ((DocumentContent.Named)doc).getName());
        }

        bldr.add(Document.Keys.content.name(), doc.getContent());
        JsonObject metadata = doc.getMetadata();
        if (metadata != null) bldr.add(Document.Keys.metadata.name(), metadata);

        if (doc instanceof DocumentContent.Annotated) {
            List<? extends Annotation> anns =
                ((DocumentContent.Annotated)doc).getAnnotations();

            JsonArrayBuilder array = JSON_BF.createArrayBuilder();
            for (Annotation ann : anns) array.add(toJson(ann));
            bldr.add(Document.Keys.annotations.name(), array);
        }

        return bldr.build();
    }

    /**
     * Creates a JsonObject from an Annotation
     *
     * @param ann An annotation instance, either a new annotation (no UUID)
     *            to create on the API, or an existing annotation to update.
     * @return JSON payload to send to server.
     */
    static JsonObject toJson(Annotation ann) throws IOException {
        JsonObjectBuilder bldr = JSON_BF.createObjectBuilder();
        bldr.add(Annotation.Keys.is_active.name(), ann.active);

        if (ann.uuid != null)
            bldr.add(Annotation.Keys.uuid.name(), ann.uuid.toString());
        if (ann.userID != null)
            bldr.add(Annotation.Keys.user_id.name(), ann.userID.toString());

        if (ann instanceof Annotation.Assignment)
            return toJson(bldr, (Annotation.Assignment)ann);
        else if (ann instanceof Annotation.Judgment)
            return toJson(bldr, (Annotation.Judgment)ann);
        else
            throw new IllegalArgumentException("Invalid annotation class");
    }

    /**
     * Generate a JSON payload for an assignment annotation
     */
    private static JsonObject toJson(JsonObjectBuilder bldr,
          Annotation.Assignment ann) throws IOException {
        /* the API accepts flat task / label names as well as JSON hashes
         * with a "name" key. use the flat format here. */
        bldr.add(Annotation.Keys.task.name(), ann.label.getTask().getName());
        bldr.add(Annotation.Keys.label.name(), ann.label.getName());
        bldr.add(Annotation.Keys.provenance.name(), ann.provenance.name());
        bldr.add(Annotation.Keys.is_negated.name(), ann.negativeExample);
        bldr.add(Annotation.Keys.is_trainable.name(), ann.trainable);
        if (!Double.isNaN(ann.confidence))
            bldr.add(Annotation.Keys.confidence.name(), ann.confidence);

        if (ann instanceof Annotation.SpanAssignment) {
            Annotation.SpanAssignment span = (Annotation.SpanAssignment)ann;
            bldr.add(Annotation.Keys.offset.name(), span.offset);
            bldr.add(Annotation.Keys.length.name(), span.length);
            bldr.add(Annotation.Keys.text.name(), span.getText());
        }

        return bldr.build();
    }

    /**
     * Generate a JSON payload for a judgment annotation
     */
    private static JsonObject toJson(JsonObjectBuilder bldr,
          Annotation.Judgment ann) {
        if (ann.assignment.uuid == null)
            throw new IllegalStateException("Can't judge uncommitted assignment");

        bldr.add(Annotation.Keys.subject_id.name(),
                 ann.assignment.uuid.toString());

        bldr.add(Annotation.Keys.is_negated.name(), ann.disagreement);
        return bldr.build();
    }

    /**
     * Expands a compacted JSON hash of document data into standard form.
     *
     * @param compact The original, compact document.
     * @returns The expanded document
     */
    static JsonObject expandDocument(JsonObject compact) {
        JsonObjectBuilder expander = JSON_BF.createObjectBuilder();
        JsonObject doc = compact.getJsonObject("document");

        // copy over all of the non-annotation keys to the new document
        for (Map.Entry<String, JsonValue> entry : doc.entrySet()) {
            if (!entry.getKey().equals(Document.Keys.annotations.name()))
                expander.add(entry.getKey(), entry.getValue());
        }

        // expand all of the annotations
        JsonArray anns = doc.getJsonArray(Document.Keys.annotations.name());
        if (anns != null) {
            JsonArrayBuilder arr = JSON_BF.createArrayBuilder();
            for (JsonValue a : anns) arr.add(expandAnnotation((JsonObject)a));
            expander.add(Document.Keys.annotations.name(), arr);
        }

        return JSON_BF.createObjectBuilder()
            .add("document", expander.build()).build();
    }

    /**
     * Estimates the size of a JSON value.
     *
     * When large batches are transmitted, it is important to limit the batch
     * size to avoid transmission errors. This routine will estimate (generally
     * under-estimate) the size of a JSON value so that batch generators have
     * an idea when to stop.
     */
    static long estimateSizeOfJson(JsonValue json) {
        if (json instanceof JsonArray) {
            // 2 for the square brackets
            return estimateSizeOfJson((JsonArray)json);
        } else if (json instanceof JsonString) {
            return estimateSizeOfJson((JsonString)json);
        } else if (json instanceof JsonObject) {
            // 2 for the curly braces
            return estimateSizeOfJson((JsonObject)json);
        } else if (json instanceof JsonNumber) {
            // really rough estimate for typical size of int vs double, etc.
            return 7;
        } else if (json == JsonValue.TRUE) {
            return 4;
        } else if (json == JsonValue.FALSE) {
            return 5;
        } else if (json == JsonValue.NULL || json == null) {
            return 4;
        } else {
            throw new UnsupportedOperationException("Unknown value type");
        }
    }

    /**
     * Estimate the size of a JSON-serialized hash
     */
    private static long estimateSizeOfJson(JsonObject hash) {
        long sizeOf = 2; // curly braces
        for (Map.Entry<String, JsonValue> entry : hash.entrySet()) {
            sizeOf += 3; // quotes for the key and the colon separator
            /* assume (incorrectly, but true more often than not) that
             * keys are only ASCII code points */
            sizeOf += entry.getKey().length();
            sizeOf += estimateSizeOfJson(entry.getValue());
        }
        return sizeOf + Math.max(0, hash.size() - 1); // comma separators
    }

    /**
     * Estimate the size of a JSON-serialized array
     */
    private static long estimateSizeOfJson(JsonArray array) {
        long sizeOf = 2;   // square brackets
        for (JsonValue v : array) sizeOf += estimateSizeOfJson(v);
        return sizeOf + Math.max(0, array.size() - 1); // comma separators
    }

    /**
     * Estimate the size of a JSON-serialized string
     */
    private static long estimateSizeOfJson(JsonString string) {
        return 2 + jsonUtf8SizeOf(string.getChars()); // double quotes
    }

    /**
     * Compute the size of a String in UTF-8 encoded bytes.
     *
     * Surrogate pairs need special handling, because they are terrib\b\b\b\b\b
     * encoded as 6-character escapes, other then that the table is just a
     * mapping of unicode code point to the number of bytes in the UTF-8
     * encoding of code points in that range.
     */
    private static long jsonUtf8SizeOf(CharSequence seq) {
        long sizeOf = 0;
        for (int i = 0; i < seq.length(); i++) {
            // determine the UTF-8 encoded size of the string
            char ch = seq.charAt(i);
            if (ch < 0x80) sizeOf += 1;
            // most european characters
            else if (ch < 0x800) sizeOf += 2;
            // low surrogate pairs
            else if (ch >= 0xd800 && ch <= 0xdbff) sizeOf += 6;
            // high surrogate pairs
            else if (ch >= 0xdc00 && ch <= 0xdfff) sizeOf += 6;
            // ideographs and other code points < 0x10000
            else sizeOf += 3;
        }
        return sizeOf;
    }

    /**
     * Parses an ISO-8601-formatted string into a Java Date instance.
     */
    static Date parseDate(String iso8601) {
        if (iso8601 == null) return null;
        /* SimpleDateFormat doesn't handle time zones so well; fortunately
         * we only need to worry about Zulu */
        try {
            return FORMATTER_CACHE.get().parse(iso8601.replace("Z", "GMT+00:00"));
        } catch (ParseException ex) {
            throw new IllegalStateException("Invalid date in document", ex);
        }
    }

    /**
     * Common JsonReaderFactory for this package.
     */
    static final JsonBuilderFactory JSON_BF = Json.createBuilderFactory(null);

    static final JsonObject EMPTY_JSON_OBJECT =
        JSON_BF.createObjectBuilder().build();

    /**
     * Expands a compacted JSON hash for an annotation into standard form.
     *
     * @param compact The input, compacted annotation
     *
     * @returns JsonObject of the expanded annotation
     */
    private static JsonObject expandAnnotation(JsonObject compact) {
        JsonObjectBuilder expander = JSON_BF.createObjectBuilder();
        for (Map.Entry<String, JsonValue> entry : compact.entrySet()) {
            Annotation.Keys annKey = COMPACT_ANN_KEYS.get(entry.getKey());
            if (isAnnotationObjectNameKey(annKey)) {
                expander.add(annKey.name(), JSON_BF.createObjectBuilder()
                             .add("name", entry.getValue()).build());
            } else {
                expander.add(annKey.name(), entry.getValue());
            }
        }
        return expander.build();
    }

    /**
     * Returns true if the annotation key should be an object with a
     * single sub-key ('name'), rather than a direct-copy of the value.
     * Helper method for expandAnnotation.
     */
    private static boolean isAnnotationObjectNameKey(Annotation.Keys key) {
        return key == Annotation.Keys.label ||
            key == Annotation.Keys.task;
    }

    // Cache of SimpleDateFormat instances, to avoid re-creation costs
    private static final ThreadLocal<SimpleDateFormat> FORMATTER_CACHE =
      new ThreadLocal<SimpleDateFormat>() {
        @Override protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }
      };

    // Table mapping keys (strings) in compact format annotations into full keys
    private static final Map<String, Annotation.Keys> COMPACT_ANN_KEYS;

    static {
        HashMap<String, Annotation.Keys> annMap =
            new HashMap<String, Annotation.Keys>();
        annMap.put("a", Annotation.Keys.uuid);
        annMap.put("b", Annotation.Keys.task);
        annMap.put("c", Annotation.Keys.label);
        annMap.put("d", Annotation.Keys.offset);
        annMap.put("e", Annotation.Keys.length);
        annMap.put("f", Annotation.Keys.text);
        annMap.put("g", Annotation.Keys.offset2);
        annMap.put("h", Annotation.Keys.length2);
        annMap.put("i", Annotation.Keys.text2);
        annMap.put("j", Annotation.Keys.is_active);
        annMap.put("k", Annotation.Keys.boost);
        annMap.put("l", Annotation.Keys.confidence);
        annMap.put("m", Annotation.Keys.provenance);
        annMap.put("n", Annotation.Keys.created_at);
        annMap.put("o", Annotation.Keys.updated_at);
        annMap.put("p", Annotation.Keys.status);
        annMap.put("q", Annotation.Keys.importance);
        annMap.put("r", Annotation.Keys.user_id);
        annMap.put("s", Annotation.Keys.is_trainable);
        annMap.put("t", Annotation.Keys.requested_for);
        annMap.put("u", Annotation.Keys.queued_at);
        annMap.put("v", Annotation.Keys.pending_at);
        annMap.put("w", Annotation.Keys.subject_id);
        annMap.put("x", Annotation.Keys.is_in_agreement);
        annMap.put("y", Annotation.Keys.is_negated);
        COMPACT_ANN_KEYS = Collections.unmodifiableMap(annMap);
    }
}
