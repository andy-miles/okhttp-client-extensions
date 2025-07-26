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

import com.google.gson.Gson;
import lombok.SneakyThrows;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import java.io.IOException;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HttpJsonLoggingInterceptorTest {
    @Mock
    private Interceptor.Chain mockChain;
    @Mock
    private Request mockRequest;
    @Mock
    private Response mockResponse;
    @Mock
    private Gson mockGson;
    @Mock
    private Logger mockLogger;
    @Mock
    private LoggingEventBuilder mockLoggingEventBuilder;
    private HttpJsonLoggingInterceptor interceptorUnderTest;

    @BeforeEach
    @SneakyThrows
    public void setUpChain() {
        lenient().when(mockChain.request()).thenReturn(mockRequest);
        lenient().when(mockChain.proceed(any(Request.class))).thenReturn(mockResponse);
    }

    @BeforeEach
    @SneakyThrows
    public void setUpLogger() {
        lenient().when(mockLogger.atLevel(any(Level.class))).thenReturn(mockLoggingEventBuilder);
    }

    //////////////
    // intercept
    //////////////

    @Test
    public void intercept_withNullChain_shouldThrowException() {
        interceptorUnderTest = HttpJsonLoggingInterceptor.builder()
                .isResponseLogged(false)
                .gson(mockGson)
                .log(mockLogger)
                .build();

        assertThrows(NullPointerException.class, () -> interceptorUnderTest.intercept(null));
    }

    @Test
    @SneakyThrows
    public void intercept_withRequestOnly_shouldLogRequest() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .isResponseLogged(false)
                .gson(mockGson)
                .log(mockLogger)
                .build());
        configureMockRequest();
        doReturn("UrlValue").when(interceptorUnderTest).redactUrl(any(HttpUrl.class));
        doReturn("HeaderValue").when(interceptorUnderTest).redactHeaders(any(Headers.class));
        doReturn("RequestBody").when(interceptorUnderTest).getBodyAsString(any(Request.class));

        final Response actual = interceptorUnderTest.intercept(mockChain);

        assertAll(
                () -> assertEquals(mockResponse, actual),
                () -> verify(mockLoggingEventBuilder).log(
                        eq("\nRequest\n  URL: {}\n HEADERS: {}\n Body: {}"),
                        eq("UrlValue"),
                        eq("HeaderValue"),
                        eq("RequestBody")));
    }

    @Test
    @SneakyThrows
    public void intercept_withRequestAndIOException_shouldThrowException() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .isResponseLogged(false)
                .gson(mockGson)
                .log(mockLogger)
                .build());
        configureMockRequest();
        doReturn("UrlValue").when(interceptorUnderTest).redactUrl(any(HttpUrl.class));
        doReturn("HeaderValue").when(interceptorUnderTest).redactHeaders(any(Headers.class));
        doThrow(new IOException("Exception")).when(interceptorUnderTest).getBodyAsString(any(Request.class));

        assertThrows(IOException.class, () -> interceptorUnderTest.intercept(mockChain));
    }

    @Test
    @SneakyThrows
    public void intercept_withResponse_shouldLogResponse() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .isRequestLogged(false)
                .gson(mockGson)
                .log(mockLogger)
                .build());
        configureMockResponse();
        doReturn("HeaderValue").when(interceptorUnderTest).redactHeaders(any(Headers.class));
        final Pair<String, Response> parsedBody = Pair.of("BodyValue", mockResponse);
        doReturn(parsedBody).when(interceptorUnderTest).extractResponseBodyAsString(any(Response.class));

        final Response actual = interceptorUnderTest.intercept(mockChain);

        assertAll(
                () -> assertEquals(mockResponse, actual),
                () -> {
                    final ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
                    verify(mockLoggingEventBuilder).log(
                            eq("{}\n HEADERS:{}\n BODY:\n{}"),
                            codeCaptor.capture(),
                            eq("HeaderValue"),
                            eq("BodyValue"));
                    final String codeCaptorValue = codeCaptor.getValue();
                    assertTrue(StringUtils.contains(codeCaptorValue, "HTTP Response (")
                            && StringUtils.contains(codeCaptorValue, "HTTP/2"));
                });
    }

    @Test
    @SneakyThrows
    public void intercept_withResponseAndIOException_shouldThrowException() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .isRequestLogged(false)
                .gson(mockGson)
                .log(mockLogger)
                .build());
        doThrow(new IOException("Exception"))
                .when(interceptorUnderTest)
                .extractResponseBodyAsString(any(Response.class));

        assertThrows(IOException.class, () -> interceptorUnderTest.intercept(mockChain));
    }

    ////////////////////
    // getBodyAsString
    ////////////////////

    @Test
    @SneakyThrows
    public void getBodyAsString_withNullRequest_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        assertEquals("[No Body]", interceptorUnderTest.getBodyAsString(null));
    }

    @Test
    @SneakyThrows
    public void getBodyAsString_withNullRequestBody_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        when(mockRequest.body()).thenReturn(null);
        assertEquals("[No Body]", interceptorUnderTest.getBodyAsString(mockRequest));
    }

    @Test
    @SneakyThrows
    public void getBodyAsString_withUnsupportedContentType_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        configureNewRequestBody("image", "png");

        final String actual = interceptorUnderTest.getBodyAsString(mockRequest);

        assertEquals("[Unsupported content type: image/png]", actual);
    }

    @Test
    @SneakyThrows
    public void getBodyAsString_withUnsupportedApplicationSubType_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        configureNewRequestBody("application", "unsupported");

        final String actual = interceptorUnderTest.getBodyAsString(mockRequest);

        assertEquals("[Unsupported content type: application/unsupported]", actual);
    }

    @Test
    @SneakyThrows
    public void getBodyAsString_withJsonBody_shouldReturnBody() {
        configureInterceptorForMethods();
        configureNewRequestBody("application", "json");
        doReturn("JsonValue").when(interceptorUnderTest).gsonify(anyString());
        final Buffer mockBuffer = mock(Buffer.class);
        when(mockBuffer.readUtf8()).thenReturn("BodyContent");
        doReturn(mockBuffer).when(interceptorUnderTest).newBuffer();

         final String actual = interceptorUnderTest.getBodyAsString(mockRequest);

         assertEquals("JsonValue", actual);
    }

    ////////////////////////////////
    // extractResponseBodyAsString
    ////////////////////////////////

    @Test
    @SneakyThrows
    public void extractResponseBodyAsString_withNullResponse_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        final Pair<String, Response> expected = Pair.of("[No Body]", null);

        final Pair<String, Response> actual = interceptorUnderTest.extractResponseBodyAsString(null);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void extractResponseBodyAsString_withNullResponseBody_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        when(mockResponse.body()).thenReturn(null);
        final Pair<String, Response> expected = Pair.of("[No Body]", mockResponse);

        final Pair<String, Response> actual = interceptorUnderTest.extractResponseBodyAsString(mockResponse);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void extractResponseBodyAsString_withUnsupportedContentType_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        configureNewResponseBody("image", "png");
        final Pair<String, Response> expected = Pair.of("[Unsupported content type: image/png]", mockResponse);

        final Pair<String, Response> actual = interceptorUnderTest.extractResponseBodyAsString(mockResponse);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void extractResponseBodyAsString_withUnsupportedApplicationSubType_shouldReturnExpectedValue() {
        configureInterceptorForMethods();
        configureNewResponseBody("application", "unsupported");
        final Pair<String, Response> expected =
                Pair.of("[Unsupported content type: application/unsupported]", mockResponse);

        final Pair<String, Response> actual = interceptorUnderTest.extractResponseBodyAsString(mockResponse);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void extractResponseBodyAsString_withJsonFormattedGzipEncodedBody_shouldReturnExpectedResponse() {
        configureInterceptorForMethods();
        configureNewResponseBody("application", "json");
        when(mockResponse.header(eq(CONTENT_ENCODING))).thenReturn("gzip");
        final BufferedSource mockBufferedSource = mock(BufferedSource.class);
        when(mockBufferedSource.readUtf8()).thenReturn("OriginalJsonFormattedBody");
        doReturn(mockBufferedSource).when(interceptorUnderTest).newBufferedSource(any(ResponseBody.class));
        doReturn("JsonFormattedBody").when(interceptorUnderTest).gsonify(anyString());
        final Response mockWrappedResponse = mock(Response.class);
        final Response.Builder mockBuilder = configureMockResponseBuilder(mockWrappedResponse);

        final ResponseBody mockDeflatedResponseBody = mock(ResponseBody.class);
        try (final MockedStatic<ResponseBody> bodyMockedStatic = mockStatic(ResponseBody.class)) {
            bodyMockedStatic.when(() -> ResponseBody.create(
                            any(MediaType.class),
                            anyString()))
                    .thenReturn(mockDeflatedResponseBody);

            final Pair<String, Response> actual = interceptorUnderTest.extractResponseBodyAsString(mockResponse);

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals("JsonFormattedBody", actual.getLeft()),
                    () -> assertEquals(mockWrappedResponse, actual.getRight()),
                    () -> verify(mockBuilder).removeHeader(CONTENT_ENCODING),
                    () -> verify(mockBuilder).body(eq(mockDeflatedResponseBody)));
        }
    }

    @Test
    @SneakyThrows
    public void extractResponseBodyAsString_withJsonFormattedBody_shouldReturnExpectedResposne() {
        configureInterceptorForMethods();
        final ResponseBody mockResponseBody = configureNewResponseBody("application", "json");
        when(mockResponseBody.string()).thenReturn("OriginalJsonFormattedBody");
        doReturn("JsonFormattedBody").when(interceptorUnderTest).gsonify(anyString());
        final Response mockWrappedResponse = mock(Response.class);
        final Response.Builder mockBuilder = configureMockResponseBuilder(mockWrappedResponse);

        final ResponseBody mockDeflatedResponseBody = mock(ResponseBody.class);
        try (final MockedStatic<ResponseBody> bodyMockedStatic = mockStatic(ResponseBody.class)) {
            bodyMockedStatic.when(() -> ResponseBody.create(
                            any(MediaType.class),
                            anyString()))
                    .thenReturn(mockDeflatedResponseBody);

            final Pair<String, Response> actual = interceptorUnderTest.extractResponseBodyAsString(mockResponse);

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals("JsonFormattedBody", actual.getLeft()),
                    () -> assertEquals(mockWrappedResponse, actual.getRight()),
                    () -> verify(mockBuilder).body(eq(mockDeflatedResponseBody)));
        }
    }

    //////////////////
    // redactHeaders
    //////////////////

    @Test
    public void redactHeaders_withNullHeaders_shouldReturnExpectedResponse() {
        configureInterceptorForMethods();
        assertEquals("  No Headers", interceptorUnderTest.redactHeaders(null));
    }

    @Test
    public void redactHeaders_withEmptyHeaders_shouldReturnExpectedResponse() {
        configureInterceptorForMethods();
        final Headers mockHeaders = mock(Headers.class);
        when(mockHeaders.size()).thenReturn(0);

        assertEquals("  No Headers", interceptorUnderTest.redactHeaders(mockHeaders));
    }

    @Test
    public void redactHeaders_withNoMatchingRedactedHeaders_shouldReturnAllHeaders() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .redactedHeader("RedactedHeader")
                .gson(mockGson)
                .log(mockLogger)
                .build());
        final Headers headers = Headers.of(
                "Header1", "Value1",
                "Header2", "Value2");

        final String actual = interceptorUnderTest.redactHeaders(headers);

        assertAll(
                () -> assertTrue(StringUtils.contains(actual, "Header1")),
                () -> assertTrue(StringUtils.contains(actual, "Value1")),
                () -> assertTrue(StringUtils.contains(actual, "Header2")),
                () -> assertTrue(StringUtils.contains(actual, "Value2")));
    }

    @Test
    public void redactHeaders_withMatchingRedactedHeaders_shouldReturnSanitizedHeaders() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .redactedHeader("Header2")
                .gson(mockGson)
                .log(mockLogger)
                .build());
        final Headers headers = Headers.of(
                "Header1", "Value1",
                "Header2", "Value2");

        final String actual = interceptorUnderTest.redactHeaders(headers);

        assertAll(
                () -> assertTrue(StringUtils.contains(actual, "Header1")),
                () -> assertTrue(StringUtils.contains(actual, "Value1")),
                () -> assertTrue(StringUtils.contains(actual, "Header2")),
                () -> assertFalse(StringUtils.contains(actual, "Value2")),
                () -> assertTrue(StringUtils.contains(actual, " **********")));
    }

    //////////////
    // redactUrl
    //////////////

    @Test
    public void redactUrl_withNullUrl_shouldThrowException() {
        configureInterceptorForMethods();
        assertThrows(NullPointerException.class, () -> interceptorUnderTest.redactUrl(null));
    }

    @Test
    public void redactUrl_withNoQueryParameters_shouldReturnUrl() {
        configureInterceptorForMethods();
        final HttpUrl url = HttpUrl.parse("https://www.someurl.com");

        final String actual = interceptorUnderTest.redactUrl(url);

        assertEquals("https://www.someurl.com/", actual);
    }

    @Test
    public void redactUrl_withNoRedactedQueryParameters_shouldReturnUrl() {
        configureInterceptorForMethods();
        final HttpUrl url = HttpUrl.parse("https://www.someurl.com/api?q1=v1&q2=v2");

        final String actual = interceptorUnderTest.redactUrl(url);

        assertEquals("https://www.someurl.com/api?q1=v1&q2=v2", actual);
    }

    @Test
    public void redactUrl_withRedactedQueryParameter_shouldReturnSanitizedUrl() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .redactedQueryParam("q1")
                .gson(mockGson)
                .log(mockLogger)
                .build());
        final HttpUrl url = HttpUrl.parse("https://www.someurl.com/api?q1=v1&q2=v2");

        final String actual = interceptorUnderTest.redactUrl(url);

        assertEquals("https://www.someurl.com/api?q1=REDACTED&q2=v2", actual);
    }

    private HttpJsonLoggingInterceptor configureInterceptorForMethods() {
        interceptorUnderTest = spy(HttpJsonLoggingInterceptor.builder()
                .gson(mockGson)
                .log(mockLogger)
                .build());
        return interceptorUnderTest;
    }

    private ResponseBody configureNewResponseBody(final String type, final String subType) {
        final MediaType mockMediaType = mock(MediaType.class);
        when(mockMediaType.type()).thenReturn(type);
        lenient().when(mockMediaType.subtype()).thenReturn(subType);
        lenient().when(mockMediaType.toString()).thenReturn(type + "/" + subType);

        final ResponseBody mockRequestBody = mock(ResponseBody.class);
        when(mockRequestBody.contentType()).thenReturn(mockMediaType);

        when(mockResponse.body()).thenReturn(mockRequestBody);

        return mockRequestBody;
    }

    private RequestBody configureNewRequestBody(final String type, final String subType) {
        final MediaType mockMediaType = mock(MediaType.class);
        when(mockMediaType.type()).thenReturn(type);
        lenient().when(mockMediaType.subtype()).thenReturn(subType);
        lenient().when(mockMediaType.toString()).thenReturn(type + "/" + subType);

        final RequestBody mockRequestBody = mock(RequestBody.class);
        when(mockRequestBody.contentType()).thenReturn(mockMediaType);

        when(mockRequest.body()).thenReturn(mockRequestBody);

        return mockRequestBody;
    }

    private Response configureMockResponse() {
        final Protocol mockProtocol = mock(Protocol.class);
        lenient().when(mockProtocol.name()).thenReturn("HTTP_2");
        lenient().when(mockResponse.protocol()).thenReturn(mockProtocol);
        lenient().when(mockResponse.code()).thenReturn(200);

        final Headers mockHeaders = mock(Headers.class);
        lenient().when(mockResponse.headers()).thenReturn(mockHeaders);

        return mockResponse;
    }

    private Response.Builder configureMockResponseBuilder(final Response responseToReturn) {
        final Response.Builder mockBuilder = mock(Response.Builder.class);
        lenient().when(mockBuilder.removeHeader(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.body(any(ResponseBody.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(responseToReturn);
        when(mockResponse.newBuilder()).thenReturn(mockBuilder);

        return mockBuilder;
    }

    private Request configureMockRequest() {
        final HttpUrl mockUrl = mock(HttpUrl.class);
        when(mockRequest.url()).thenReturn(mockUrl);

        final Headers mockHeaders = mock(Headers.class);
        when(mockRequest.headers()).thenReturn(mockHeaders);

        return mockRequest;
    }
}
