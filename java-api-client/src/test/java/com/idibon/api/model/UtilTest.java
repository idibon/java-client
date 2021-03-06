/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import java.util.UUID;
import java.util.Collections;

import org.junit.*;
import javax.json.*;

import java.io.StringReader;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class UtilTest {

    @Test public void testSerializeAssignment() throws Exception {
        Collection mockCollection = Collection.instance(null, "collection");
        Document mockDocument = mockCollection.document("document");
        Label mockLabel = mockCollection.task("task").label("label");
        UUID mockUser = UUID.fromString("00000000-0000-0000-0000-000000000001");

        Annotation.DocumentAssignment annotation =
            new Annotation.DocumentAssignment(
                mockDocument, null, false, mockLabel,
                Annotation.Provenance.Human, "final", true, false, 1.0,
                null, null, mockUser, Collections.<JsonObject>emptyList());

        JsonObject serialized = Util.toJson(annotation);
        assertThat(serialized.getString("user_id"), is(mockUser.toString()));
        assertThat(serialized.getString("status"), is("final"));
        assertThat(serialized.getString("provenance"), is("Human"));
        assertThat(serialized.getBoolean("is_active"), is(false));
        assertThat(serialized.getBoolean("is_negated"), is(true));
        assertThat(serialized.getJsonNumber("confidence").doubleValue(), is(1.0));
        assertThat(serialized.getBoolean("is_trainable"), is(false));
    }

    @Test public void testJsonUtf8SizeOf() throws Exception {
        java.lang.reflect.Method method = Util.class.
            getDeclaredMethod("jsonUtf8SizeOf", CharSequence.class);
        method.setAccessible(true);

        assertThat(((Long)method.invoke(null, "今日わ")).longValue(), is(9L));
        assertThat(((Long)method.invoke(null, "hello")).longValue(), is(5L));
        assertThat(((Long)method.invoke(null, "hèllo")).longValue(), is(6L));
        char high = 0xd83d;
        char low = 0xdca9;
        String pileOfPoo = "" + high + low;
        assertThat(((Long)method.invoke(null, pileOfPoo)).longValue(), is(12L));
    }

    @Test public void testEstimateSizeOfJsonArray() throws Exception {
        java.lang.reflect.Method method = Util.class.
            getDeclaredMethod("estimateSizeOfJson", JsonArray.class);
        method.setAccessible(true);

        String json = "[[0,1],true,[],false,\"hi\"]";
        JsonArray array = Json.createReader(new StringReader(json)).readArray();
        assertThat(((Long)method.invoke(null, array)).longValue(), is(38L));
    }

    @Test public void testEstimateSizeOfJsonHash() throws Exception {
        java.lang.reflect.Method method = Util.class.
            getDeclaredMethod("estimateSizeOfJson", JsonObject.class);
        method.setAccessible(true);

        String json = "{\"key\":{\"inner\":5,\"other\":null},\"key2\":{}}";
        JsonObject obj = Json.createReader(new StringReader(json)).readObject();
        assertThat(((Long)method.invoke(null, obj)).longValue(), is(48L));
    }

    @Test public void testExpandAnnotation() throws Exception {
        java.lang.reflect.Method method = Util.class.
            getDeclaredMethod("expandAnnotation", JsonObject.class);
        method.setAccessible(true);

        JsonObject compact = Json.createObjectBuilder()
            .add("a", "00000000-0000-0000-0000-000000000000")
            .add("b", "task-name")
            .add("c", "label-name")
            .add("d", 10)
            .add("e", 5)
            .add("j", true)
            .add("m", "Human")
            .add("p", "final")
            .add("y", false).build();

        JsonObject expand = (JsonObject)method.invoke(null, compact);
        assertThat(expand.getJsonObject("task").getString("name"), is("task-name"));
        assertThat(expand.getJsonObject("label").getString("name"), is("label-name"));
        assertThat(expand.getString("provenance"), is("Human"));
        assertThat(expand.getJsonNumber("length").intValue(), is(5));
        assertThat(expand.getBoolean("is_negated"), is(false));
    }
}
