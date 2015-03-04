/**
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.idibon.api.http.*;
import javax.json.*;

import static com.idibon.api.model.Util.*;

/**
 * A Document is the basic unit of content in a Collection
 */
public class Document extends IdibonHash
      implements Predictable, DocumentContent.Annotated, DocumentContent.Named {

    /**
     * Keys in the JSON hash. Some keys may not be present, depending
     * on how the document data was originally loaded.
     */
    public enum Keys {
        /**
         * When the document was last annotated. Returns: a date formatted
         * as an ISO-8601 JsonPrimitive string.
         */
        annotated_at,
        /**
         * Annotations on this document. Returns: a JsonArray of JsonObjects.
         */
        annotations,
        /**
         * The original document content. Returns: a JsonPrimitive string.
         */
        content,
        /**
         * The date the document was originally created. Returns: a date
         * formatted as an ISO-8601 JsonPrimitive string.
         */
        created_at,
        /**
         * The natural language features extracted from this document used
         * by a specific task. Returns: a JsonArray of JsonObjects.
         */
        features,
        /**
         * The hash of metadata included with this document. Returns: a
         * JsonObject.
         */
        metadata,
        /**
         * The document name. Returns: a JsonPrimitive string.
         */
        name,
        /**
         * The tokenized document content. Returns: a JsonArray of
         * JsonObjects.
         */
        tokens,
        /**
         * The date this document was last updated. Returns: a date formatted
         * as an ISO-8601 JsonPrimitive string.
         */
        updated_at,
        /**
         * The document UUID. Returns: a UUID formatted as a JsonPrimitive
         * string.
         */
        uuid;
    }

    /**
     * Returns the raw JsonElement at the key, if one exists.
     */
    public JsonValue get(Keys key) throws IOException {
        return getJson().get(key.name());
    }

    /**
     * Returns the document name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns the document UUID
     */
    public UUID getUUID() throws IOException {
        UUID uuid = (UUID)_jsonCache.get(Keys.uuid);

        if (uuid == null) {
            String raw = getJson().getString(Keys.uuid.name(), null);
            uuid = UUID.fromString(raw);
            _jsonCache.put(Keys.uuid, uuid);
        }

        return uuid;
    }

    /**
     * Returns one of the Date keys, or null.
     *
     * @param dateKey The date value to retrieve
     * @return The requested date, or null.
     */
    public Date getDate(Keys dateKey) throws IOException {
        if (dateKey != Keys.updated_at && dateKey != Keys.created_at &&
              dateKey != Keys.annotated_at) {
            throw new IllegalArgumentException("Not a date key");
        }

        Date date = (Date)_jsonCache.get(dateKey);
        if (date == null) {
            date = parseDate(getJson().getString(dateKey.name(), null));
            if (date != null) _jsonCache.put(dateKey, date);
        }

        return date;
    }

    /**
     * Returns the document content.
     */
    public String getContent() throws IOException {
        return getJson().getString(Keys.content.name());
    }

    /**
     * Returns the metadata associated with this document
     */
    public JsonObject getMetadata() throws IOException {
        return getJson().getJsonObject(Keys.metadata.name());
    }

    /**
     * Returns the annotations on this document
     */
    @SuppressWarnings("unchecked")
    public List<? extends Annotation> getAnnotations() throws IOException {
        List<Annotation> annotations =
            (List<Annotation>)_jsonCache.get(Keys.annotations);

        if (annotations != null)
            return annotations;

        JsonArray jsonAnns = getJson().getJsonArray(Keys.annotations.name());
        if (annotations == null || annotations.isEmpty())
            return Collections.EMPTY_LIST;

        annotations = new ArrayList<>(annotations.size());

        /* map of annotation subject ID to the annotations referencing it.
         * since subject_id may also include document IDs, this isn't
         * sufficient to determine if an annotation is an assignment or
         * a judgment */
        Map<String, List<JsonObject>> judgments = new HashMap<>();

        /* keep track of actual annotations. if an annotation has a subject
         * ID, and the subject ID is in this set of UUIDs, then the annotation
         * is a judgment. */
        Set<String> annUUIDs = new HashSet<>();

        for (JsonObject ann :jsonAnns.getValuesAs(JsonObject.class)) {
            annUUIDs.add(ann.getString(Annotation.Keys.uuid.name()));
            String sub = ann.getString(Annotation.Keys.subject_id.name(), null);
            if (sub != null) {
                List<JsonObject> list = judgments.get(sub);
                if (list == null) {
                    list = new ArrayList<JsonObject>();
                    judgments.put(sub, list);
                }
                list.add(ann);
            }
        }

        // iterate over the assignments, and reify Annotation instances
        for (JsonObject ann : jsonAnns.getValuesAs(JsonObject.class)) {
            String sub = ann.getString(Annotation.Keys.subject_id.name(), null);

            if (sub != null && annUUIDs.contains(sub)) {
                // this is a judgment. it will be handled by the assignment
                continue;
            }

            String uuid = ann.getString(Annotation.Keys.uuid.name());
            List<JsonObject> annJudgments = judgments.get(uuid);
            if (annJudgments == null) annJudgments = Collections.EMPTY_LIST;
            annotations.add(Annotation.Assignment.parse(this, ann, annJudgments));
        }

        _jsonCache.put(Keys.annotations, annotations);
        return annotations;
    }

    /**
     * Returns a body with {"document":"name"}
     */
    public JsonObject createPredictionRequest() {
        return JSON_BF.createObjectBuilder().add("document", getName()).build();
    }

    /**
     * Forces cached JSON data to be reloaded from the server.
     */
    @SuppressWarnings("unchecked")
    @Override public Document invalidate() {
        super.invalidate();
        _jsonCache = null;
        return this;
    }

    /**
     * Returns the raw JSON data for this Document
     */
    public JsonObject getJson() throws IOException {
        JsonObject result = super.getJson(null).getJsonObject("document");
        /* this is racy, but the only negative outcome is that multiple
         * threads may each instantiate a ConcurrentHashMap, and all but
         * one of the instances will be GCd immediately. */
        if (_jsonCache == null)
            _jsonCache = new ConcurrentHashMap<>();
        return result;
    }

    static Document instance(Collection parent, String name) {
        return new Document(name, parent, parent.getInterface());
    }

    static Document instance(Collection parent, JsonObject obj) {
        String name = obj.getJsonObject("document").getString("name");
        return instance(parent, name).preload(obj);
    }

    private Document(String name, Collection parent, HttpInterface httpIntf) {
        super(parent.getEndpoint() + "/" + percentEncode(name), httpIntf);
        _name = name;
        _parent = parent;
    }

    private final Collection _parent;
    private final String _name;

    /* Simple cache of data loaded from the JsonObject, to avoid expensive
     * repetitive parsing. */
    private volatile Map<Keys, Object> _jsonCache;
}
