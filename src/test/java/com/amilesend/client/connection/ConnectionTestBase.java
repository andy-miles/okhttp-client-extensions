/*
 * okhttp-client-extensions - A set of helpful extensions to support okhttp clients
 * Copyright © 2025-2026 Andy Miles (andy.miles@amilesend.com)
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
import com.amilesend.client.connection.retry.Retriable;
import com.amilesend.client.connection.retry.RetriableCallResponse;
import com.amilesend.client.connection.retry.RetryStrategy;
import com.amilesend.client.parse.GsonFactoryBase;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConnectionTestBase {
    protected static final String USER_AGENT_VALUE = "ConnectionTest/1.0";
    protected static final int SUCCESS_RESPONSE_CODE = 200;
    protected static final int REQUEST_ERROR_CODE = 403;
    protected static final int THROTTLED_ERROR_CODE = 429;
    protected static final int SERVER_ERROR_RESPONSE_CODE = 503;

    @Mock
    protected OkHttpClient mockHttpClient;
    @Mock
    protected Gson mockGson;
    @Mock
    protected GsonFactoryBase mockGsonFactory;
    @Mock
    protected AuthManager mockAuthManager;
    @Mock
    protected RetryStrategy mockRetryStrategy;
    protected Connection<GsonFactoryBase> connectionUnderTest;

    @BeforeEach
    public void setUp() {
        connectionUnderTest = spy(new DefaultConnectionBuilder()
                .httpClient(mockHttpClient)
                .gsonFactory(mockGsonFactory)
                .authManager(mockAuthManager)
                .baseUrl("http://baseurl")
                .userAgent(USER_AGENT_VALUE)
                .isGzipContentEncodingEnabled(true)
                .retryStrategy(mockRetryStrategy)
                .build());
    }

    protected Response newMockedResponse(final int code, final Long retryAfterSeconds) {
        final Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(code);
        lenient().when(mockResponse.isSuccessful()).thenReturn(String.valueOf(code).startsWith("2"));
        if (retryAfterSeconds != null) {
            lenient().when(mockResponse.header(eq("Retry-After"))).thenReturn(String.valueOf(retryAfterSeconds));
        }

        return mockResponse;
    }

    protected Response newMockedResponse(final int code) {
        final ResponseBody mockBody = mock(ResponseBody.class);
        lenient().when(mockBody.byteStream()).thenReturn(mock(InputStream.class));
        lenient().when(mockBody.source()).thenReturn(mock(BufferedSource.class));

        final Response mockResponse = mock(Response.class);
        lenient().when(mockResponse.code()).thenReturn(code);
        lenient().when(mockResponse.isSuccessful()).thenReturn(code == SUCCESS_RESPONSE_CODE);
        lenient().when(mockResponse.body()).thenReturn(mockBody);

        return mockResponse;
    }

    @SneakyThrows
    protected Response setUpHttpClientMock(final Response mockResponse) {
        final RetriableCallResponse mockRetriableResponse = mock(RetriableCallResponse.class);
        when(mockRetriableResponse.isSuccess()).thenReturn(true);
        when(mockRetriableResponse.getResponse()).thenReturn(mockResponse);
        when(mockRetryStrategy.invoke(any(Retriable.class))).thenReturn(mockRetriableResponse);

        return mockResponse;
    }

    @SneakyThrows
    protected void setUpHttpClientMock(final IOException ioException) {
        final RetriableCallResponse mockRetriableResponse = mock(RetriableCallResponse.class);
        when(mockRetriableResponse.isSuccess()).thenReturn(false);
        when(mockRetriableResponse.getExceptions()).thenReturn(Collections.singletonList(ioException));
        when(mockRetryStrategy.invoke(any(Retriable.class))).thenReturn(mockRetriableResponse);
    }
}
