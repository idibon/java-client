/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http.impl;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.util.concurrent.*;
import java.nio.charset.Charset;

import com.idibon.api.http.*;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import com.idibon.api.util.ExtendedByteArrayOutputStream;

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
    public Future<JsonElement> httpGet(String endpoint) throws IOException {
        return httpGet(endpoint, null);
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpGet(String, JsonElement)}
     */
    public Future<JsonElement> httpGet(final String endpoint,
                                       final JsonElement body) throws IOException {

        Callable<JsonElement> async = new Callable<JsonElement>() {
            public JsonElement call() throws IOException {
                HttpURLConnection conn = getConnection(endpoint);
                conn.setDoInput(true);
                conn.setDoOutput(body != null);
                conn.setRequestMethod("GET");

                OutputStream os = null;
                try {
                    if (body != null) {
                        /* Serialize the JSON payload and set the content-length
                         * header appropriately */
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        writeJsonStream(body, bos);
                        byte[] bytes = bos.toByteArray();
                        /* Java's HttpURLConnection has a (major) bug in
                         * getOutputStream which forces the method to POST.
                         * Use the X-HTTP-Method-Override header so that the
                         * server will treat the request as a GET. */
                        conn.setRequestProperty("X-HTTP-Method-Override", "GET");
                        conn.setRequestProperty("Content-Length",
                                                Integer.toString(bytes.length));
                        os = conn.getOutputStream();
                        os.write(bytes);
                    }
                    return maybeHandleChunkedInput(conn);
                } catch (IOException ex) {
                    /* if an HTTP protocol (3xx, 4xx, 5xx) error caused this
                     * exception, convert it to a more meaningful exception tree
                     * rather than just an IOException */
                    int responseCode = -1;
                    try {
                        responseCode = conn.getResponseCode();
                    } catch (IOException _) { }

                    if (responseCode != -1 && responseCode != HTTP_OK)
                        throw HttpException.from(conn, ex);

                    // otherwise, re-throw the original exception
                    throw ex;
                } finally {
                    if (os != null) os.close();
                }
            }
        };

        FutureTask<JsonElement> future = new FutureTask<JsonElement>(async);
        try {
            _threadPool.submit(future);
            return future;
        } catch (RejectedExecutionException ex) {
            throw new IOException("Unable to perform asynchronous GET", ex);
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPut(String, JsonElement)}
     */
    public Future<JsonElement> httpPut(String endpoint, JsonElement body)
        throws IOException {

        throw new UnsupportedOperationException("Not yet!");
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPost(String, JsonElement)}
     */
    public Future<JsonElement> httpPost(String endpoint, JsonElement body)
        throws IOException {

        throw new UnsupportedOperationException("Not yet!");
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPost(String, JsonElement)}
     */
    public Future<JsonElement> httpDelete(String endpoint, JsonElement body)
        throws IOException {

        throw new UnsupportedOperationException("Not yet!");
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface@shutdown(long)}
     */
    public void shutdown(long quiesceTime) {
        _threadPool.shutdown();
        boolean clean = false;
        try {
            clean = _threadPool.awaitTermination(quiesceTime,
                                                 TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            // ignore
        } finally {
            if (!clean) _threadPool.shutdownNow();
        }
    }

    /**
     * Reads the HTTP response from the server and parses the embedded JSON.
     * If the response uses chunked encoding, each of the chunks will be
     * parsed and added to an array
     */
    private JsonElement maybeHandleChunkedInput(HttpURLConnection conn)
        throws IOException {

        InputStream is = new BufferedInputStream(conn.getInputStream());
        try {
            String transferEncoding = conn.getHeaderField("Transfer-Encoding");
            if (transferEncoding == null)
                return readJsonStream(is);

            String contentType = conn.getHeaderField("Content-Type");
            if (contentType == null)
                throw new IOException("Missing header field Content-Type");

            int boundaryIndex = contentType.indexOf("boundary=");
            if (boundaryIndex == -1)
                throw new IOException("Chunk boundary missing");

            String boundary = contentType.substring(boundaryIndex + 9);
            return handleChunkedInput(is, boundary);
        } finally {
            is.close();
        }
    }

    /**
     * Reads chunked data from the input stream, returning an array
     * of the JSON elements from each chunk.
     *
     * @param is Data stream to read
     * @param boundary The chunk boundary from the HTTP header
     */
    private static JsonElement handleChunkedInput(InputStream is,
        String boundary) throws IOException {

        byte[] sep = ("--" + boundary).getBytes(UTF8);

        JsonArray array = new JsonArray();
        ExtendedByteArrayOutputStream bs = new ExtendedByteArrayOutputStream();
        byte[] buffer = new byte[4096];

        /* read from the input stream until all data has been consumed,
         * appending the intermediate data to the byte stream */
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            bs.write(buffer, 0, len);
            /* the buffered read may result in multiple chunks existing in
             * the byte stream. process each chunk and then consume the
             * parsed bytes from the byte stream. */
            for (int i = bs.indexOf(sep); i != -1; i = bs.indexOf(sep)) {
                // don't try to read a JSON stream if there is no data to read
                if (i != 0) array.add(readJsonStream(bs.toInputStream(0, i)));
                // discard the chunk and separator that was just processed
                bs.dropFirst(i + sep.length);
            }
        }

        /* per spec, the last chunk should be demarcated by two hyphens
         * following the last boundary marker. since the last boundary marker
         * should be dropped by the loop above, the byte stream should be
         * just the two trailing hyphens (45 = ASCII for '-') */
        if (bs.size() != 2 || !bs.endsWith(new byte[]{ 45, 45 }))
            throw new IOException("Invalid chunked transfer encoding");

        return array;
    }

    /**
     * Reads the input stream as a UTF-8 encoded String and parses a JSON
     * element from it.
     *
     * @param stream InputStream to consume
     * @return parsed JSON element
     */
    private static JsonElement readJsonStream(InputStream stream)
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

        if (_serverAddress.getPort() != -1) {
            String host = String.format("%s:%d", _serverAddress.getHost(),
                                        _serverAddress.getPort());
            http.setRequestProperty("Host", host);
        } else {
            http.setRequestProperty("Host", _serverAddress.getHost());
        }

        http.setUseCaches(false);

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

    /// Asynchronous execution threads for FutureTasks
    private final ThreadPoolExecutor _threadPool =
        new ThreadPoolExecutor(3, 8, 20, TimeUnit.SECONDS,
                               new LinkedBlockingQueue<Runnable>());

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final HostnameVerifier NO_HOSTNAME_VALIDATION =
      new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
      };
}
