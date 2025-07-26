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
package com.amilesend.client.connection.http;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;

/**
 * A logging interceptor to aid in debugging.
 *
 * @see Interceptor
 */
@Builder
public class HttpJsonLoggingInterceptor implements Interceptor {
    private static final String REDACTED = " **********";
    private static final String REDACTED_QUERY_PARAM_VALUE = "REDACTED";
    private static final String GZIP_ENCODING = "gzip";

    /** The set of HTTP headers to redact in the logging statements. */
    @Singular
    private final Set<String> redactedHeaders;
    /** The set of HTTP URL query parameters to redact in the logging statements. */
    @Singular
    private final Set<String> redactedQueryParams;
    /** The configured GSON instance. Note: Recommended to set the pretty-print flag. */
    @NonNull
    private final Gson gson;
    /** Log requests flag. */
    @Builder.Default
    private final boolean isRequestLogged = true;
    /** Log response flag. */
    @Builder.Default
    private final boolean isResponseLogged = true;
    /** The logger instance. */
    @Builder.Default
    private final Logger log = LoggerFactory.getLogger(HttpJsonLoggingInterceptor.class);;
    /** The logging level of the statements. */
    @Builder.Default
    private final Level loggingLevel = Level.INFO;

    @Override
    public Response intercept(@NonNull final Chain chain) throws IOException {
        final Stopwatch watch = Stopwatch.createStarted();
        final Request request = chain.request();
        if (isRequestLogged) {
            log.atLevel(loggingLevel)
                    .log("\nRequest\n  URL: {}\n HEADERS: {}\n Body: {}",
                            redactUrl(request.url()),
                            redactHeaders(request.headers()),
                            getBodyAsString(request));
        }
        final Response response = chain.proceed(request);

        watch.stop();
        if (isResponseLogged) {
            final long responseTimeMs = watch.elapsed(TimeUnit.MILLISECONDS);
            final Pair<String, Response> bodyResponsePair = extractResponseBodyAsString(response);
            final String responseCode = new StringBuilder("\nHTTP Response (")
                    .append(response.protocol()
                            .name()
                            .replaceFirst("_", "/")
                            .replace('_', '.'))
                    .append(" ")
                    .append(response.code())
                    .append(") in ")
                    .append(responseTimeMs)
                    .append(" ms")
                    .toString();
            log.atLevel(loggingLevel)
                    .log("{}\n HEADERS:{}\n BODY:\n{}",
                            responseCode,
                            redactHeaders(response.headers()),
                            bodyResponsePair.getLeft());
            return bodyResponsePair.getRight();
        }

        return response;
    }

    @VisibleForTesting
    String getBodyAsString(final Request request) throws IOException {
        if (Objects.isNull(request) || Objects.isNull(request.body())) {
            return "[No Body]";
        }

        final RequestBody body = request.body();
        final MediaType mediaType = body.contentType();
        final String type = mediaType.type();
        final boolean isApplicationType = StringUtils.equals(type, "application");
        if (!StringUtils.equals(type, "text") && !isApplicationType) {
            return "[Unsupported content type: " + mediaType + "]";
        }

        final String subType = mediaType.subtype();
        final boolean isSubTypeJson = StringUtils.equals(subType, "json");
        if (isApplicationType && !isSubTypeJson) {
            return "[Unsupported content type: " + mediaType + "]";
        }

        final Buffer buffer = newBuffer();
        body.writeTo(buffer);

        final String bodyContent = buffer.readUtf8();
        return isSubTypeJson ? gsonify(bodyContent) : bodyContent;
    }

    @VisibleForTesting
    Buffer newBuffer() {
        return new Buffer();
    }

    @VisibleForTesting
    Pair<String, Response> extractResponseBodyAsString(final Response response) throws IOException {
        if (Objects.isNull(response) || Objects.isNull(response.body())) {
            return Pair.of("[No Body]", response);
        }

        final ResponseBody body = response.body();
        final MediaType mediaType = body.contentType();
        final String type = mediaType.type();
        final boolean isApplicationType = StringUtils.equals(type, "application");
        if (!StringUtils.equals(type, "text") && !isApplicationType) {
            return Pair.of("[Unsupported content type: " + mediaType + "]", response);
        }

        final String subType = mediaType.subtype();
        final boolean isSubTypeJson = StringUtils.equals(subType, "json");
        if (isApplicationType && !isSubTypeJson) {
            return Pair.of("[Unsupported content type: " + mediaType + "]", response);
        }

        if (GZIP_ENCODING.equals(response.header(CONTENT_ENCODING))) {
            final String bodyContent = newBufferedSource(body).readUtf8();
            final Response wrappedResponse = response.newBuilder()
                    .removeHeader(CONTENT_ENCODING)
                    .body(ResponseBody.create(mediaType, bodyContent))
                    .build();
            final String formattedBodyContent = isSubTypeJson ? gsonify(bodyContent) : bodyContent;
            return Pair.of(formattedBodyContent, wrappedResponse);
        }

        final String bodyContent = body.string();
        final Response wrappedResponse = response.newBuilder()
                .body(ResponseBody.create(mediaType, bodyContent))
                .build();
        final String formattedBodyContent = isSubTypeJson ? gsonify(bodyContent) : bodyContent;
        return Pair.of(formattedBodyContent, wrappedResponse);
    }

    @VisibleForTesting
    BufferedSource newBufferedSource(final ResponseBody body) {
        return Okio.buffer(new GzipSource(body.source()));
    }

    @VisibleForTesting
    String gsonify(final String value) {
        return gson.toJson(new JsonParser().parse(value));
    }

    @VisibleForTesting
    String redactHeaders(final Headers headers) {
        if (Objects.isNull(headers) || headers.size() < 1) {
            return "  No Headers";
        }

        final StringJoiner sj = new StringJoiner("\n  ");
        headers.forEach(p -> {
            if (redactedHeaders.contains(p.getFirst().trim())) {
                sj.add(p.getFirst().trim() + ":" + REDACTED);
            } else {
                sj.add(p.getFirst() + ": " + p.getSecond());
            }
        });

        return "\n  " + sj;
    }

    @VisibleForTesting
    String redactUrl(@NonNull final HttpUrl url) {
        if (url.querySize() == 0 || redactedQueryParams.isEmpty()) {
            return url.toString();
        }

        final HttpUrl.Builder urlBuilder = url.newBuilder().query(null);
        for (int i = 0; i < url.querySize(); ++i) {
            final String paramName = url.queryParameterName(i);
            final String paramValue = redactedQueryParams.contains(paramName)
                    ? REDACTED_QUERY_PARAM_VALUE
                    : url.queryParameterValue(i);

            urlBuilder.addQueryParameter(paramName, paramValue);
        }

        return urlBuilder.build().toString();
    }
}
