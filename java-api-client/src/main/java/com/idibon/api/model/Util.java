/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.*;
import javax.json.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

final class Util {

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
     * Expands a compacted JSON hash of document data into standard form.
     *
     * @param compact The original, compact document.
     * @returns The expanded document
     */
    static JsonObject expandDocument(JsonObject compact) {
        JsonObjectBuilder expander = JSON_BF.createObjectBuilder();

        for (Map.Entry<String, JsonValue> entry : compact.entrySet()) {
            if (!entry.getKey().equals(Document.Keys.annotations.name()))
                expander.add(entry.getKey(), entry.getValue());
        }

        JsonArray anns = compact.getJsonArray(Document.Keys.annotations.name());
        if (anns != null) {
            JsonArrayBuilder arr = JSON_BF.createArrayBuilder();
            for (JsonValue a : anns) arr.add(expandAnnotation((JsonObject)a));
            expander.add(Document.Keys.annotations.name(), arr);
        }

        return expander.build();
    }

    /**
     * Parses an ISO-8601-formatted string into a Java Date instance.
     */
    static Date parseDate(String iso8601) {
        if (iso8601 == null) return null;
        /* SimpleDateFormat doesn't handle time zones so well; fortunately
         * we only need to worry about Zulu */
        try {
            return FORMATTER_CACHE.get().parse(iso8601.replace("Z", "+00:00"));
        } catch (ParseException ex) {
            throw new IllegalStateException("Invalid date in document", ex);
        }
    }

    /**
     * Common JsonReaderFactory for this package.
     */
    static final JsonBuilderFactory JSON_BF = Json.createBuilderFactory(null);

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
            expander.add(annKey.name(), entry.getValue());
        }
        return expander.build();
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
