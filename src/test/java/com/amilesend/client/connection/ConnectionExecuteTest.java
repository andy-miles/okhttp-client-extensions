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

import com.amilesend.client.parse.parser.GsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.Data;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionExecuteTest extends ConnectionTestBase {
    /////////////////////////////////
    // Execute (Request, GsonParser)
    /////////////////////////////////

    @Test
    @SneakyThrows
    public void execute_withValidRequestAndParserAndGzipEncodedResponse_shouldReturnResponse() {
        when(mockGsonFactory.getInstance(any(Connection.class))).thenReturn(mockGson);
        doNothing().when(connectionUnderTest).validateResponseCode(any(Response.class));
        setUpHttpClientMock(setUpResponseWithBody("gzip"));

        final TestResponse testResponse = new TestResponse("response value");
        final GsonParser<TestResponse> mockParser = mock(GsonParser.class);
        when(mockParser.parse(any(Gson.class), any(InputStream.class))).thenReturn(testResponse);

        try (final MockedConstruction<GZIPInputStream> gzipMockCtor = mockConstruction(GZIPInputStream.class)) {
            final TestResponse actual = connectionUnderTest.execute(mock(Request.class), mockParser);

            assertAll(
                    () -> assertEquals(testResponse, actual),
                    () -> verify(mockParser).parse(isA(Gson.class), isA(InputStream.class)));
        }
    }

    @Test
    @SneakyThrows
    public void execute_withValidRequestAndParserAndNonGzipEncodedResponse_shouldReturnResponse() {
        when(mockGsonFactory.getInstance(any(Connection.class))).thenReturn(mockGson);
        doNothing().when(connectionUnderTest).validateResponseCode(any(Response.class));
        setUpHttpClientMock(setUpResponseWithBody(null));

        final TestResponse testResponse = new TestResponse("response value");
        final GsonParser<TestResponse> mockParser = mock(GsonParser.class);
        when(mockParser.parse(any(Gson.class), any(InputStream.class))).thenReturn(testResponse);

        final TestResponse actual = connectionUnderTest.execute(mock(Request.class), mockParser);

        assertAll(
                () -> assertEquals(testResponse, actual),
                () -> verify(mockParser).parse(isA(Gson.class), isA(InputStream.class)));
    }

    @Test
    @SneakyThrows
    public void execute_withIOException_shouldThrowException() {
        final IOException expectedCause = new IOException("Exception");
        setUpHttpClientMock(expectedCause);

        final Throwable thrown = assertThrows(RequestException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mock(GsonParser.class)));

        assertEquals(expectedCause, thrown.getCause());
    }

    @Test
    @SneakyThrows
    public void execute_withJsonParseException_shouldThrowException() {
        when(mockGsonFactory.getInstance(any(Connection.class))).thenReturn(mockGson);
        doNothing().when(connectionUnderTest).validateResponseCode(any(Response.class));
        setUpHttpClientMock(setUpResponseWithBody(null));

        final GsonParser<TestResponse> mockParser = mock(GsonParser.class);
        when(mockParser.parse(any(Gson.class), any(InputStream.class))).thenThrow(new JsonParseException("Exception"));

        final Throwable thrown = assertThrows(ResponseParseException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mockParser));

        assertInstanceOf(JsonParseException.class, thrown.getCause());
    }

    @Test
    public void execute_withInvalidInput_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.execute(null, mock(GsonParser.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.execute(mock(Request.class), null)));
    }

    //////////////////////
    // Execute (Request)
    //////////////////////

    @Test
    @SneakyThrows
    public void execute_withValidRequest_shouldReturnResponseCode() {
        doNothing().when(connectionUnderTest).validateResponseCode(any(Response.class));
        final Integer expected = Integer.valueOf(SUCCESS_RESPONSE_CODE);
        final Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(expected);
        setUpHttpClientMock(mockResponse);

        final Response actual = connectionUnderTest.execute(mock(Request.class));

        assertEquals(expected, actual.code());
    }

    @Test
    @SneakyThrows
    public void execute_withNoParserAndIOException_shouldThrowException() {
        final IOException expectedCause = new IOException("Exception");
        setUpHttpClientMock(expectedCause);

        final Throwable thrown =
                assertThrows(RequestException.class, () -> connectionUnderTest.execute(mock(Request.class)));

        assertEquals(expectedCause, thrown.getCause());
    }

    @Test
    @SneakyThrows
    public void execute_withNoParserAndInvalidRequest() {
        assertThrows(NullPointerException.class, () -> connectionUnderTest.execute(null));
    }

    private Response setUpResponseWithBody(final String contentEncoding) {
        final InputStream mockBodyStream = mock(InputStream.class);
        final ResponseBody mockResponseBody = mock(ResponseBody.class);
        when(mockResponseBody.byteStream()).thenReturn(mockBodyStream);
        final Response mockResponse = mock(Response.class);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponse.header(eq(CONTENT_ENCODING))).thenReturn(contentEncoding);

        return mockResponse;
    }

    /////////////////////////
    // validateResponseCode
    /////////////////////////

    @Test
    public void validateResponseCode_withThrottledResponse_shouldThrowException() {
        final long expectedRetryTime = 60L;
        final Response mockResponse = newMockedResponse(THROTTLED_ERROR_CODE, expectedRetryTime);

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.validateResponseCode(mockResponse));

        assertEquals(expectedRetryTime, ((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void validateResponseCode_withThrottledResponseAndNullRetryAfterHeader_shouldThrowException() {
        final Response mockResponse = newMockedResponse(THROTTLED_ERROR_CODE, (Long) null);

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.validateResponseCode(mockResponse));

        assertEquals(10L, ((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void validateResponseCode_withServerErrorResponseCode_shouldThrowException() {
        final Response mockResponse = newMockedResponse(SERVER_ERROR_RESPONSE_CODE);

        assertThrows(ResponseException.class, () -> connectionUnderTest.validateResponseCode(mockResponse));
    }

    @Test
    public void validateResponseCode_withRequestErrorResponseCode_shouldThrowException() {
        final Response mockResponse = newMockedResponse(REQUEST_ERROR_CODE);

        assertThrows(RequestException.class, () -> connectionUnderTest.validateResponseCode(mockResponse));
    }

    @Data
    public static class TestResponse {
        private final String value;
    }
}
