/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http.impl;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.nio.charset.Charset;

import com.idibon.api.http.*;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import static java.net.HttpURLConnection.*;

/**
 * Implementation of the HttpInterface using the JDK's built-in
 * HttpURLConnection transport layer.
 */
public class JdkHttpInterface implements HttpInterface {

    public static final String IDIBON_API = "https://api.idibon.com/";

    /**
     * Configure the scheme, host name, and port of the Idibon API server.
     */
    public JdkHttpInterface forServer(String serverAddress)
        throws MalformedURLException {

        return forServer(new URL(serverAddress));
    }

    public JdkHttpInterface forServer(URL serverAddress) {
        _serverAddress = serverAddress;
        return this;
    }

    /**
     * Configure a proxy server for all connections.
     */
    public JdkHttpInterface proxiedBy(Proxy proxyServer) {
        if (proxyServer == null)
            proxyServer = Proxy.NO_PROXY;
        _proxyServer = proxyServer;
        return this;
    }

    /**
     * Disable SSL hostname validation for HTTPS API servers.
     */
    public JdkHttpInterface withoutHostnameValidation() {
        _hostnameValidation = false;
        return this;
    }

    /**
     * Configure HTTP BASIC Authentication
     */
    public JdkHttpInterface withApiKey(String apiKey) {
        _apiKey = apiKey;
        return this;
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpGet(String)}
     */
    public JsonElement httpGet(String endpoint)
        throws IOException {

        HttpURLConnection conn = getConnection(endpoint);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setRequestMethod("GET");

        InputStream is = null;
        try {
            is = new BufferedInputStream(conn.getInputStream());
            return readJsonStream(is);
        } catch (IOException ex) {
            /* if an HTTP protocol (3xx, 4xx, 5xx) error caused this exception,
             * convert it to a more meaningful exception tree rather than just
             * an IOException */
            int responseCode = -1;
            try {
                responseCode = conn.getResponseCode();
            } catch (IOException ignored) { }

            if (responseCode != -1 && responseCode != HTTP_OK)
                throw HttpException.from(conn, ex);

            // otherwise, re-throw the original exception
            throw ex;
        } finally {
            if (is != null) is.close();
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpGet(String, JsonElement)}
     */
    public JsonElement httpGet(String endpoint, JsonElement body)
        throws IOException {

        HttpURLConnection conn = getConnection(endpoint);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(conn.getOutputStream());
            writeJsonStream(body, os);
            return maybeHandleChunkedInput(conn);
        } catch (IOException ex) {
            /* if an HTTP protocol (3xx, 4xx, 5xx) error caused this exception,
             * convert it to a more meaningful exception tree rather than just
             * an IOException */
            int responseCode = -1;
            try {
                responseCode = conn.getResponseCode();
            } catch (IOException ignored) { }

            if (responseCode != -1 && responseCode != HTTP_OK)
                throw HttpException.from(conn, ex);

            // otherwise, re-throw the original exception
            throw ex;
        } finally {
            if (os != null) os.close();
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPut(String, JsonElement)}
     */
    public JsonElement httpPut(String endpoint, JsonElement body)
        throws IOException {

        throw new UnsupportedOperationException("Not yet!");
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPost(String, JsonElement)}
     */
    public JsonElement httpPost(String endpoint, JsonElement body)
        throws IOException {

        throw new UnsupportedOperationException("Not yet!");
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPost(String, JsonElement)}
     */
    public JsonElement httpDelete(String endpoint, JsonElement body)
        throws IOException {

        throw new UnsupportedOperationException("Not yet!");
    }

    private JsonElement maybeHandleChunkedInput(HttpURLConnection conn)
        throws IOException {

        InputStream is = new BufferedInputStream(conn.getInputStream());
        try {
            String transferEncoding = conn.getHeaderField("Transfer-Encoding");
            if (transferEncoding == null)
                return readJsonStream(is);

            JsonArray array = new JsonArray();
            throw new UnsupportedOperationException("Chunked encoding");
        } finally {
            is.close();
        }
    }

    /**
     * Reads the input stream as a UTF-8 encoded String and parses a JSON
     * element from it.
     *
     * @param stream InputStream to consume
     * @return parsed JSON element
     */
    private JsonElement readJsonStream(InputStream stream)
        throws IOException {

        JsonParser parser = new JsonParser();
        try {
            return parser.parse(new InputStreamReader(stream, UTF8));
        } catch (JsonParseException ex) {
            throw new IOException("Unable to decode JSON from stream", ex);
        }
    }

    /**
     * Encodes the JSON payload as a UTF-8 string and writes the bitstream
     * to the output stream.
     *
     * @param body The JSON payload to write
     * @param stream Where the payload should be written
     */
    private void writeJsonStream(JsonElement body, OutputStream stream)
        throws IOException {

        if (body == null)
            throw new NullPointerException("body");

        if (stream == null)
            throw new NullPointerException("stream");

        JsonWriter writer = new JsonWriter(
            new OutputStreamWriter(stream, UTF8));

        try {
            new Gson().toJson(body, writer);
            writer.flush();
        } catch (JsonIOException ex) {
            throw new IOException("Unable to encode JSON to stream", ex);
        }
    }

    /**
     * Returns an HttpURLConnection for the requested endpoing on the
     * configured API server.
     */
    private HttpURLConnection getConnection(String endpoint)
        throws IOException {

        if (endpoint == null)
            throw new NullPointerException("endpoint");

        if (_serverAddress == null)
            throw new IllegalStateException("server address not configured");

        if (endpoint.isEmpty() || endpoint.charAt(0) != '/')
            throw new IllegalArgumentException("endpoint is not a valid path");

        URL fullUrl = new URL(_serverAddress.getProtocol(),
            _serverAddress.getHost(), _serverAddress.getPort(), endpoint);

        URLConnection conn = fullUrl.openConnection(_proxyServer);

        if (!(conn instanceof HttpURLConnection))
            throw new IllegalStateException("Server is not HTTP/HTTPS");

        // configure SSL-specific parameters
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection https = (HttpsURLConnection)conn;
            HostnameVerifier verifier;
            if (_hostnameValidation)
                verifier = HttpsURLConnection.getDefaultHostnameVerifier();
            else
                verifier = NO_HOSTNAME_VALIDATION;
            https.setHostnameVerifier(verifier);
        }

        HttpURLConnection http = (HttpURLConnection)conn;
        if (_apiKey != null) {
            // BASIC auth, with the API key as the username and no password
            String credentials = base64Encode(_apiKey + ":");
            http.setRequestProperty("Authorization", "Basic " + credentials);
        }

        return http;
    }

    private static String base64Encode(String raw) {
        byte[] utf8 = raw.getBytes(UTF8);
        String base64 = "";
        for (int i = 0; i < utf8.length - 2; i += 3) {
            int v = ((int)utf8[i] & 0xff) << 16;
            v |= ((int)utf8[i + 1] & 0xff) << 8;
            v |= ((int)utf8[i + 2] & 0xff);
            base64 += BASE64_TAB.charAt((v >> 18) & 0x3f);
            base64 += BASE64_TAB.charAt((v >> 12) & 0x3f);
            base64 += BASE64_TAB.charAt((v >> 6) & 0x3f);
            base64 += BASE64_TAB.charAt(v & 0x3f);
        }
        switch (utf8.length % 3) {
        case 1: {
            int v = ((int)utf8[utf8.length - 1] & 0xff);
            base64 += BASE64_TAB.charAt((v >> 2) & 0x3f);
            base64 += BASE64_TAB.charAt((v << 4) & 0x3f);
            base64 += "==";
            break;
        }
        case 2: {
            int v = ((int)utf8[utf8.length - 2] & 0xff) << 8;
            v |= ((int)utf8[utf8.length - 1] & 0xff);
            base64 += BASE64_TAB.charAt((v >> 10) & 0x3f);
            base64 += BASE64_TAB.charAt((v >> 4) & 0x3f);
            base64 += BASE64_TAB.charAt((v << 2) & 0x3f);
            base64 += "=";
            break;
        }
        default:
            break;
        }
        return base64;
    }

    private static final String BASE64_TAB =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    /// Scheme, hostname and port of the API server
    private URL _serverAddress;

    /// Proxy server used for the connection, or Proxy.NO_PROXY for direct
    private Proxy _proxyServer = Proxy.NO_PROXY;

    /// Perform SSL hostname validation when true and scheme is https
    private boolean _hostnameValidation = true;

    /// The API Key used for authentication, or null if no auth needed.
    private String _apiKey = null;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final HostnameVerifier NO_HOSTNAME_VALIDATION =
      new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
      };
}
