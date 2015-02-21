/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import static java.net.HttpURLConnection.*;

public class HttpException extends java.io.IOException {

    public HttpException(HttpURLConnection conn, Throwable chained) {
        this(conn.getURL(), getResponseCode(conn), getResponseMessage(conn),
             parseJsonErrorStream(conn), chained);
    }

    public HttpException(URL errorUrl, int responseCode, String responseMessage,
                         JsonElement errorInfo, Throwable chained) {
        super(responseMessage, chained);
        _errorUrl = errorUrl;
        _responseCode = responseCode;
        _errorInfo = errorInfo;
    }

    /**
     * Return the URL for the request that generated the failure.
     */
    public URL getFailedURL() { return _errorUrl; }

    /**
     * Return the HTTP response code for the failed request.
     */
    public int getHttpResponseCode() { return _responseCode; }

    /**
     * Returns the parsed JSON element that the server provided in the error
     * body, if present.
     */
    public JsonElement getJsonErrorInfo() { return _errorInfo; }

    /**
     * Generic superclass for all 4xx error codes
     */
    public static class ClientError extends HttpException {
        public ClientError(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 400
     */
    public static class BadRequest extends ClientError {
        public BadRequest(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 401
     */
    public static class Unauthorized extends ClientError {
        public Unauthorized(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 403
     */
    public static class Forbidden extends ClientError {
        public Forbidden(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 404
     */
    public static class NotFound extends ClientError {
        public NotFound(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 413
     */
    public static class EntityTooLarge extends ClientError {
        public EntityTooLarge(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * Generic super class for all 5xx error codes
     */
    public static class ServerError extends HttpException {
        public ServerError(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 500
     */
    public static class InternalServerError extends ServerError {
        public InternalServerError(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 503
     */
    public static class ServiceUnavailable extends ServerError {
        public ServiceUnavailable(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * HTTP 504
     */
    public static class GatewayTimeout extends ServerError {
        public GatewayTimeout(HttpURLConnection conn, Throwable chained) {
            super(conn, chained);
        }
    }

    /**
     * Generate specific exception instances for known HTTP response codes
     */
    public static HttpException from(HttpURLConnection conn, Throwable chain) {
        int code = -1;
        try {
            code = conn.getResponseCode();
        } catch (IOException ignored) { }

        switch (code) {
        case HTTP_BAD_REQUEST:
            return new HttpException.BadRequest(conn, chain);
        case HTTP_UNAUTHORIZED:
            return new HttpException.Unauthorized(conn, chain);
        case HTTP_FORBIDDEN:
            return new HttpException.Forbidden(conn, chain);
        case HTTP_NOT_FOUND:
            return new HttpException.NotFound(conn, chain);
        case HTTP_ENTITY_TOO_LARGE:
            return new HttpException.EntityTooLarge(conn, chain);
        case HTTP_INTERNAL_ERROR:
            return new HttpException.InternalServerError(conn, chain);
        case HTTP_UNAVAILABLE:
            return new HttpException.ServiceUnavailable(conn, chain);
        case HTTP_GATEWAY_TIMEOUT:
            return new HttpException.GatewayTimeout(conn, chain);
        }

        if (code >= 400 && code < 500)
            return new HttpException.ClientError(conn, chain);
        else if (code >= 500 && code < 600)
            return new HttpException.ServerError(conn, chain);

        return new HttpException(conn, chain);
    }

    /**
     * Suppresses exceptions on {@link java.net.HttpURLConnection#getResponseCode}
     */
    private static int getResponseCode(HttpURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException ex) {
            return -1;
        }
    }

    /**
     * Suppresses exceptions on {@link java.net.HttpURLConnection#getResponseMessage}
     */
    private static String getResponseMessage(HttpURLConnection conn) {
        try {
            return conn.getResponseMessage();
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * Parses a JSON content body following a failed HTTP request, if present.
     */
    private static JsonElement parseJsonErrorStream(HttpURLConnection conn) {
        if (conn.getContentType().equals("application/json")) {
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                try {
                    JsonParser p = new JsonParser();
                    return p.parse(new InputStreamReader(errorStream, "UTF-8"));
                } catch (Exception ex) {
                    /* suppress all exceptions here, since this method is only
                     * called during the HttpException construction process */
                } finally {
                    try { errorStream.close(); } catch (IOException ignored) { }
                }
            }
        }
        return null;
    }

    private final URL _errorUrl;
    private final int _responseCode;
    private final JsonElement _errorInfo;
}
