/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http.impl;

import java.io.*;
import java.net.InetAddress;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import org.junit.*;
import com.google.gson.*;

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
        try {
            intf.httpGet("/").get();
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    @Test public void returnJsonErrorBody() throws Throwable {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget)
            .withApiKey("NOT-A-KEY");
        try {
            try {
                intf.httpGet("/").get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        } catch (HttpException.Unauthorized ex) {
            JsonElement jsonInfo = ex.getJsonErrorInfo();
            assertThat(jsonInfo, is(instanceOf(JsonObject.class)));
            String msg = ((JsonObject)jsonInfo).getAsJsonPrimitive("errors")
                .getAsString();
            assertThat(msg, is("improperly formatted API key"));
        }
    }

    @Test(expected = HttpException.NotFound.class)
    public void collectionDoesNotExist() throws Throwable {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget)
            .withApiKey(_apiKey);
        try {
            intf.httpGet("/this_collection_does_not_exist/*").get();
        } catch (ExecutionException ex) {
            throw ex.getCause();
        }
    }

    @Test public void hostnameNotInCertificate() throws Throwable {
        String addr = InetAddress.getByName("api.idibon.com").getHostAddress();
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer("https://" + addr)
            .withApiKey(_apiKey);

        try {
            try {
                intf.httpGet("/").get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        } catch (javax.net.ssl.SSLHandshakeException ex) {
            Throwable cause = ex.getCause();
            assertThat(cause, is(instanceOf(java.security.cert.CertificateException.class)));
            String reason = cause.getMessage();
            assertThat(reason, startsWith("No subject alternative names"));
        }

        intf.withoutHostnameValidation().httpGet("/");
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
        JsonElement result = (JsonElement)method.invoke(intf, input, boundary);

        assertThat(result, is(instanceOf(JsonArray.class)));

        JsonArray resultArray = (JsonArray)result;
        assertThat(resultArray.size(), is(3));

        for (int i = 0; i < resultArray.size(); i++) {
            JsonElement e = resultArray.get(i);
            assertThat(e, is(instanceOf(JsonObject.class)));
            assertThat(((JsonObject)e).get("a").getAsLong(), is((long)Math.pow(10, i)));
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
