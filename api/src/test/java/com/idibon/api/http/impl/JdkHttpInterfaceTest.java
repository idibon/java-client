/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http.impl;

import java.io.*;
import java.net.InetAddress;

import org.junit.*;
import com.google.gson.*;

import com.idibon.api.http.HttpException;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class JdkHttpInterfaceTest {

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
    public void withoutAuthentication() throws Exception {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget);
        intf.httpGet("/");
    }

    @Test public void returnJsonErrorBody() throws Exception {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget)
            .withApiKey("NOT-A-KEY");
        try {
            intf.httpGet("/");
        } catch (HttpException.Unauthorized ex) {
            JsonElement jsonInfo = ex.getJsonErrorInfo();
            assertThat(jsonInfo, is(instanceOf(JsonObject.class)));
            String msg = ((JsonObject)jsonInfo).getAsJsonPrimitive("errors")
                .getAsString();
            assertThat(msg, is("improperly formatted API key"));
        }
    }

    @Test(expected = HttpException.NotFound.class)
    public void collectionDoesNotExist() throws Exception {
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer(_apiTarget)
            .withApiKey(_apiKey);
        intf.httpGet("/this_collection_does_not_exist/*");
    }

    @Test public void hostnameNotInCertificate() throws Exception {
        String addr = InetAddress.getByName("api.idibon.com").getHostAddress();
        JdkHttpInterface intf = new JdkHttpInterface()
            .forServer("https://" + addr)
            .withApiKey(_apiKey);

        try {
            intf.httpGet("/");
        } catch (javax.net.ssl.SSLHandshakeException ex) {
            Throwable cause = ex.getCause();
            assertThat(cause, is(instanceOf(java.security.cert.CertificateException.class)));
            String reason = cause.getMessage();
            assertThat(reason, startsWith("No subject alternative names"));
        }

        intf.withoutHostnameValidation().httpGet("/");
    }

}
