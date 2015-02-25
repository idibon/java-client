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

import javax.json.*;

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
    public Future<JsonValue> httpGet(String endpoint) throws IOException {
        return httpGet(endpoint, null);
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpGet(String, JsonObject)}
     */
    public Future<JsonValue> httpGet(String endpoint,
                                     JsonObject body) throws IOException {

        try {
            return _threadPool.submit(new HttpOp("GET", endpoint, body));
        } catch (RejectedExecutionException ex) {
            throw new IOException("Unable to perform asynchronous GET", ex);
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPut(String, JsonObject)}
     */
    public Future<JsonValue> httpPut(String endpoint, JsonObject body)
        throws IOException {
        try {
            return _threadPool.submit(new HttpOp("PUT", endpoint, body));
        } catch (RejectedExecutionException ex) {
            throw new IOException("Unable to perform asynchronous PUT", ex);
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPost(String, JsonObject)}
     */
    public Future<JsonValue> httpPost(String endpoint, JsonObject body)
        throws IOException {
        try {
            return _threadPool.submit(new HttpOp("POST", endpoint, body));
        } catch (RejectedExecutionException ex) {
            throw new IOException("Unable to perform asynchronous POST", ex);
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#httpPost(String, JsonObject)}
     */
    public Future<JsonValue> httpDelete(String endpoint, JsonObject body)
        throws IOException {
        try {
            return _threadPool.submit(new HttpOp("DELETE", endpoint, body));
        } catch (RejectedExecutionException ex) {
            throw new IOException("Unable to perform asynchronous DELETE", ex);
        }
    }

    /**
     * Implements {@link com.idibon.api.http.HttpInterface#shutdown(long)}
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
    private JsonValue maybeHandleChunkedInput(HttpURLConnection conn)
        throws IOException {

        try (InputStream is = new BufferedInputStream(conn.getInputStream())) {
            String transferEncoding = conn.getHeaderField("Transfer-Encoding");
            if (transferEncoding == null)
                return readJson(is);

            String contentType = conn.getHeaderField("Content-Type");
            if (contentType == null)
                throw new IOException("Missing header field Content-Type");

            int boundaryIndex = contentType.indexOf("boundary=");
            if (boundaryIndex == -1)
                throw new IOException("Chunk boundary missing");

            /* strip off the "boundary=" prefix from the header, leaving just
             * the random boundary marker text. */
            String boundary = contentType.substring(boundaryIndex + 9);
            return handleChunkedInput(is, boundary);
        }
    }

    /**
     * Reads chunked data from the input stream, returning an array
     * of the JSON elements from each chunk.
     *
     * @param is Data stream to read
     * @param boundary The chunk boundary from the HTTP header
     */
    private static JsonValue handleChunkedInput(InputStream is,
        String boundary) throws IOException {

        byte[] sep = ("--" + boundary).getBytes(UTF8);

        JsonArrayBuilder array = Json.createArrayBuilder();
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
                if (i != 0) array.add(readJson(bs.toInputStream(0, i)));
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

        return array.build();
    }

    /**
     * Reads the input stream as a UTF-8 encoded String and parses a JSON
     * element from it.
     *
     * @param stream InputStream to consume
     * @return parsed JSON element
     */
    private static JsonValue readJson(InputStream stream) {
        try (JsonReader r = JSON_RF.createReader(stream, UTF8)) {
            return r.read();
        }
    }

    /**
     * Encodes the JSON payload as a UTF-8 string and writes the bitstream
     * to the output stream.
     *
     * @param body The JSON payload to write
     * @param stream Where the payload should be written
     */
    private static <T extends OutputStream> T writeJson(JsonObject body, T os) {
        try (JsonWriter w = JSON_WF.createWriter(os, UTF8)) {
            w.writeObject(body);
        }
        return os;
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
            base64 += BASE64_TABLE.charAt((v >> 18) & 0x3f);
            base64 += BASE64_TABLE.charAt((v >> 12) & 0x3f);
            base64 += BASE64_TABLE.charAt((v >> 6) & 0x3f);
            base64 += BASE64_TABLE.charAt(v & 0x3f);
        }
        switch (utf8.length % 3) {
        case 1: {
            int v = ((int)utf8[utf8.length - 1] & 0xff);
            base64 += BASE64_TABLE.charAt((v >> 2) & 0x3f);
            base64 += BASE64_TABLE.charAt((v << 4) & 0x3f);
            base64 += "==";
            break;
        }
        case 2: {
            int v = ((int)utf8[utf8.length - 2] & 0xff) << 8;
            v |= ((int)utf8[utf8.length - 1] & 0xff);
            base64 += BASE64_TABLE.charAt((v >> 10) & 0x3f);
            base64 += BASE64_TABLE.charAt((v >> 4) & 0x3f);
            base64 += BASE64_TABLE.charAt((v << 2) & 0x3f);
            base64 += "=";
            break;
        }
        default:
            break;
        }
        return base64;
    }

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

    /// Base-64 encoding table
    private static final String BASE64_TABLE =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static final JsonWriterFactory JSON_WF = Json.createWriterFactory(null);
    private static final JsonReaderFactory JSON_RF = Json.createReaderFactory(null);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final HostnameVerifier NO_HOSTNAME_VALIDATION =
      new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
      };

    /**
     * HTTP operation with result.
     */
    private class HttpOp implements Callable<JsonValue> {
        HttpOp(String method, String endpoint, JsonObject body) {
            _method = method;
            _endpoint = endpoint;
            _body = toBytes(body);
        }

        private byte[] toBytes(JsonObject body) {
            if (body == null) return null;
            return writeJson(body, new ByteArrayOutputStream()).toByteArray();
        }

        public JsonValue call() throws IOException {
            HttpURLConnection conn = getConnection(_endpoint);
            conn.setDoInput(true);
            conn.setDoOutput(_body != null);
            /* Java's HttpURLConnection has a (major) bug in getOutputStream
             * which forces every method except PUT or POST to become a POST
             * method if an entity is included in the request. Detect this
             * case and use X-HTTP-Method-Override to work-around this
             * limitation, since the Idibon API expects bodies for some GET
             * and DELETE operations */
            if (_method.equals("PUT") || _method.equals("POST"))
                conn.setRequestMethod(_method);
            else
                conn.setRequestMethod(_body == null ? _method : "POST");

            if (_body != null) {
                conn.setRequestProperty("X-HTTP-Method-Override", _method);
                conn.setRequestProperty("Content-Length",
                                        Integer.toString(_body.length));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(_body);
                }
            }

            try {
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
                    throw httpException(conn, ex);

                // otherwise, re-throw the original exception
                throw ex;
            }
        }

        private String _endpoint;
        private String _method;
        private byte[] _body;
    }

    /**
     * Generate specific exception instances for known HTTP response codes
     */
    private static HttpException httpException(HttpURLConnection conn,
                                               Throwable chain) {
        int code = -1;
        String msg = "";
        JsonObject obj = null;
        URL url = conn.getURL();
        try {
            code = conn.getResponseCode();
            msg = conn.getResponseMessage();
            if (conn.getContentType().equals("application/json"))
                obj = (JsonObject)readJson(conn.getErrorStream());
        } catch (Exception _) { }

        switch (code) {
        case HTTP_BAD_REQUEST:
            return new HttpException.BadRequest(url, code, msg, obj, chain);
        case HTTP_UNAUTHORIZED:
            return new HttpException.Unauthorized(url, code, msg, obj, chain);
        case HTTP_FORBIDDEN:
            return new HttpException.Forbidden(url, code, msg, obj, chain);
        case HTTP_NOT_FOUND:
            return new HttpException.NotFound(url, code, msg, obj, chain);
        case HTTP_ENTITY_TOO_LARGE:
            return new HttpException.EntityTooLarge(url, code, msg, obj, chain);
        case HTTP_INTERNAL_ERROR:
            return new HttpException.InternalServerError(url, code, msg, obj, chain);
        case HTTP_UNAVAILABLE:
            return new HttpException.ServiceUnavailable(url, code, msg, obj, chain);
        case HTTP_GATEWAY_TIMEOUT:
            return new HttpException.GatewayTimeout(url, code, msg, obj, chain);
        }

        if (code >= 400 && code < 500)
            return new HttpException.ClientError(url, code, msg, obj, chain);
        else if (code >= 500 && code < 600)
            return new HttpException.ServerError(url, code, msg, obj, chain);

        return new HttpException(url, code, msg, obj, chain);
    }
}
