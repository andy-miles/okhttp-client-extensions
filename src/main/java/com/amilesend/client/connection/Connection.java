/*
 * okhttp-client-extensions - A set of helpful extensions to support okhttp clients
 * Copyright © 2025 Andy Miles (andy.miles@amilesend.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.amilesend.client.connection;

import com.amilesend.client.connection.auth.AuthManager;
import com.amilesend.client.parse.GsonFactoryBase;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.client.util.StringUtils;
import com.amilesend.client.util.VisibleForTesting;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.amilesend.client.connection.Connection.Headers.ACCEPT;
import static com.amilesend.client.connection.Connection.Headers.ACCEPT_ENCODING;
import static com.amilesend.client.connection.Connection.Headers.CONTENT_ENCODING;
import static com.amilesend.client.connection.Connection.Headers.USER_AGENT;

/** Wraps an {@link OkHttpClient} that manages parsing responses to corresponding POJO types. */
@SuperBuilder
@Getter
@Slf4j
public class Connection<G extends GsonFactoryBase> {
    public static final String FORM_DATA_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final MediaType FORM_DATA_MEDIA_TYPE = MediaType.parse(FORM_DATA_CONTENT_TYPE);
    public static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse(JSON_CONTENT_TYPE);

    protected static final String THROTTLED_RETRY_AFTER_HEADER = "Retry-After";
    protected static final String GZIP_ENCODING = "gzip";
    protected static final Long DEFAULT_RETRY_AFTER_SECONDS = Long.valueOf(10L);
    protected static final int THROTTLED_RESPONSE_CODE = 429;

    /** The underlying http client. */
    @NonNull
    private final OkHttpClient httpClient;
    /** The Gson factory used to create GSON instance that marshals request and responses to/from JSON. */
    @NonNull
    private final G gsonFactory;
    /** The authorization manager used to authenticate and sign requests. */
    @NonNull
    private final AuthManager<?> authManager;
    /** The base URL for the Graph API. */
    @NonNull
    private final String baseUrl;
    /** The user agent to include in request headers. */
    @NonNull
    private final String userAgent;
    private final boolean isGzipContentEncodingEnabled;

    /**
     * Creates a new {@link Request.Builder} with pre-configured headers for request that expect a JSON-formatted
     * response body.
     *
     * @return the request builder
     */
    public Request.Builder newRequestBuilder() {
        final Request.Builder requestBuilder = new Request.Builder()
                .addHeader(USER_AGENT, userAgent)
                .addHeader(ACCEPT, JSON_CONTENT_TYPE);
        if (isGzipContentEncodingEnabled) {
            requestBuilder.addHeader(ACCEPT_ENCODING, GZIP_ENCODING);
        }

        return authManager.addAuthentication(requestBuilder);
    }

    /**
     * Executes the given {@link Request} and parses the JSON-formatted response with given {@link GsonParser}.
     *
     * @param request the request
     * @param parser the parser to decode the response body
     * @return the response as a POJO resource type
     * @param <T> the POJO resource type
     * @throws ConnectionException if an error occurred during the transaction
     */
    public <T> T execute(@NonNull final Request request, @NonNull final GsonParser<T> parser)
            throws ConnectionException {
        try {
            try (final Response response = execute(request)) {
                final InputStream responseBodyInputStream =
                        "gzip".equals(response.header(CONTENT_ENCODING))
                                ? new GZIPInputStream(response.body().byteStream())
                                : response.body().byteStream();
                return parser.parse(gsonFactory.getInstance(this), responseBodyInputStream);
            }
        } catch (final IOException ex) {
            throw new RequestException("Unable to execute request: " + ex.getMessage(), ex);
        } catch (final JsonParseException ex) {
            throw new ResponseParseException("Error parsing response: " + ex.getMessage(), ex);
        }
    }

    /**
     * Executes the given {@link Request} and returns the associated HTTP response code. This is typically used for
     * transactions that do not expect a response in the body.
     *
     * @param request the request
     * @return the HTTP response
     * @throws ConnectionException if an error occurred during the transaction
     */
    public Response execute(@NonNull final Request request) throws ConnectionException {
        try {
            final Response response = httpClient.newCall(request).execute();
            validateResponseCode(response);
            return response;
        } catch (final IOException ex) {
            throw new RequestException("Unable to execute request: " + ex.getMessage(), ex);
        }
    }

    protected void validateResponseCode(final Response response) {
        final int code = response.code();
        if (code == THROTTLED_RESPONSE_CODE) {
            final Long retryAfterSeconds = extractRetryAfterHeaderValue(response);
            final String msg = retryAfterSeconds != null
                    ? "Request throttled. Retry after " + retryAfterSeconds + " seconds"
                    : "Request throttled";
            throw new ThrottledException(msg, retryAfterSeconds);
        }

        final boolean isRequestError = String.valueOf(code).startsWith("4");
        if (isRequestError) {
            throw new RequestException("Error with request (" + code + "): " + response);
        } else if (!response.isSuccessful()) {
            throw new ResponseException("Unsuccessful response (" + code + "): " + response);
        }
    }

    @VisibleForTesting
    protected Long extractRetryAfterHeaderValue(final Response response) {
        final String retryAfterHeaderValue = response.header(THROTTLED_RETRY_AFTER_HEADER);
        return StringUtils.isNotBlank(retryAfterHeaderValue)
                ? Long.valueOf(retryAfterHeaderValue)
                : DEFAULT_RETRY_AFTER_SECONDS;
    }

    @UtilityClass
    public static class Headers {
        public static final String ACCEPT = "Accept";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String USER_AGENT = "User-Agent";
    }
}
