/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.model;

import org.junit.*;
import javax.json.*;

import java.io.StringReader;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class UtilTest {

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
}
