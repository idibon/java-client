/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http.impl;

import java.io.*;
import java.net.InetAddress;

import org.junit.*;
import javax.json.*;

import com.idibon.api.util.Either;
import com.idibon.api.http.HttpException;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class JdkHttpInterfaceIT {

    private static String _apiKey;
    private static String _apiTarget;

    @BeforeClass public static void configure() {
        _apiKey = System.getenv("IDIBON_API_KEY");
        if (_apiKey == null)
            throw new NullPointerException("Missing IDIBON_API_KEY");

        _apiTarget = System.getenv("IDIBON_API");
        if (_apiTarget == null || _apiTarget.isEmpty())
            _apiTarget = "https://api.idibon.com/";
    }

    @Test(expected = HttpException.Unauthorized.class)
    public void withoutAuthentication() throws Throwable {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget);

        Either<IOException, JsonValue> v = intf.httpGet("/").get();
        assertTrue(v.isLeft());
        throw v.left;
    }

    @Test public void returnJsonErrorBody() throws Throwable {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget)
            .withApiKey("NOT-A-KEY");

        Either<IOException, JsonValue> v = intf.httpGet("/").get();
        assertTrue(v.isLeft());
        assertThat(v.left, is(instanceOf(HttpException.Unauthorized.class)));
        HttpException.Unauthorized ex = (HttpException.Unauthorized)v.left;
        JsonObject jsonInfo = ex.getJsonErrorInfo();
        String msg = jsonInfo.getString("errors");
        assertThat(msg, is("improperly formatted API key"));
    }

    @Test(expected = HttpException.NotFound.class)
    public void collectionDoesNotExist() throws Throwable {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget)
            .withApiKey(_apiKey);

        Either<IOException, JsonValue> v =
            intf.httpGet("/this_collection_does_not_exist/*").get();
        assertTrue(v.isLeft());
        throw v.left;
    }

    @Test public void hostnameNotInCertificate() throws Throwable {
        // Parse out the 'https://' and any trailing slashes from the API link
        // since we want it to take the unsecured path
        String apiTargetBase = _apiTarget.replaceAll("^https://([^/]+)/?$", "$1");

        String addr = InetAddress.getByName(apiTargetBase).getHostAddress();
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer("https://" + addr)
            .withApiKey(_apiKey);

        Either<IOException, JsonValue> v = intf.httpGet("/").get();
        assertTrue(v.isLeft());
        assertThat(v.left, is(instanceOf(javax.net.ssl.SSLHandshakeException.class)));
        Throwable cause = v.left.getCause();
        assertThat(cause, is(instanceOf(java.security.cert.CertificateException.class)));
        String reason = cause.getMessage();
        assertThat(reason, startsWith("No subject alternative names"));

        assertTrue(intf.withoutHostnameValidation().httpGet("/").get().isRight());
    }

    @Test public void handleChunkedInput() throws Exception {
        JdkHttpInterface intf = new JdkHttpInterface();
        java.lang.reflect.Method method = JdkHttpInterface.class.
            getDeclaredMethod("handleChunkedInput",
                              InputStream.class, String.class);
        method.setAccessible(true);
        String boundary = "BBB";

        String chunkedJSON = "{\"a\":1}--BBB{\"a\":10}--BBB{\"a\":100}--BBB--";
        InputStream input = new ByteArrayInputStream(chunkedJSON.getBytes("UTF-8"));
        JsonArray resultArray = (JsonArray)method.invoke(intf, input, boundary);

        assertThat(resultArray.size(), is(3));

        for (int i = 0; i < resultArray.size(); i++) {
            JsonValue e = resultArray.get(i);
            assertThat(e, is(instanceOf(JsonObject.class)));
            assertThat(((JsonObject)e).getJsonNumber("a").longValue(),
                       is((long)Math.pow(10, i)));
        }
    }

    /* verify that an exception is thrown if the last chunk does not have the
     * spec-defined two trailing hyphens */
    @Test(expected = IOException.class)
    public void handleBadChunkedInput() throws Throwable {
        JdkHttpInterface intf = new JdkHttpInterface();
        java.lang.reflect.Method method = JdkHttpInterface.class.
            getDeclaredMethod("handleChunkedInput",
                              InputStream.class, String.class);
        method.setAccessible(true);
        String boundary = "BBB";
        String broken = "--BBB";
        InputStream input = new ByteArrayInputStream(broken.getBytes("UTF-8"));
        try {
            method.invoke(intf, input, boundary);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
