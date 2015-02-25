/*
 * Copyright (c) 2015, Idibon, Inc.
 */
package com.idibon.api.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.JsonObject;

public class HttpException extends java.io.IOException {

    public HttpException(URL errorUrl, int responseCode, String responseMessage,
                         JsonObject errorInfo, Throwable chained) {
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
    public JsonObject getJsonErrorInfo() { return _errorInfo; }

    /**
     * Generic superclass for all 4xx error codes
     */
    public static class ClientError extends HttpException {
        public ClientError(URL errorUrl, int responseCode, String message,
                           JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 400
     */
    public static class BadRequest extends ClientError {
        public BadRequest(URL errorUrl, int responseCode, String message,
                          JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 401
     */
    public static class Unauthorized extends ClientError {
        public Unauthorized(URL errorUrl, int responseCode, String message,
                          JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 403
     */
    public static class Forbidden extends ClientError {
        public Forbidden(URL errorUrl, int responseCode, String message,
                         JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 404
     */
    public static class NotFound extends ClientError {
        public NotFound(URL errorUrl, int responseCode, String message,
                        JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 413
     */
    public static class EntityTooLarge extends ClientError {
        public EntityTooLarge(URL errorUrl, int responseCode, String message,
                              JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * Generic super class for all 5xx error codes
     */
    public static class ServerError extends HttpException {
        public ServerError(URL errorUrl, int responseCode, String message,
                           JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 500
     */
    public static class InternalServerError extends ServerError {
        public InternalServerError(URL errorUrl, int responseCode, String message,
                                   JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 503
     */
    public static class ServiceUnavailable extends ServerError {
        public ServiceUnavailable(URL errorUrl, int responseCode, String message,
                                  JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    /**
     * HTTP 504
     */
    public static class GatewayTimeout extends ServerError {
        public GatewayTimeout(URL errorUrl, int responseCode, String message,
                              JsonObject errorInfo, Throwable chained) {
            super(errorUrl, responseCode, message, errorInfo, chained);
        }
    }

    private final URL _errorUrl;
    private final int _responseCode;
    private final JsonObject _errorInfo;
}
