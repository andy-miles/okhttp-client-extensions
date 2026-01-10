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
package com.amilesend.client;

import com.amilesend.client.connection.http.OkHttpClientBuilder;
import com.amilesend.client.data.SerializedResource;
import lombok.SneakyThrows;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/** Defines the base class to set up a mock web server to test the client implementation. */
public class FunctionalTestBase {
    public static final int SUCCESS_STATUS_CODE = 200;
    public static final int USER_ERROR_CODE = 404;
    public static final int SERVICE_ERROR_CODE = 503;

    private MockWebServer mockWebServer = new MockWebServer();
    private OkHttpClient httpClient;

    @SneakyThrows
    @BeforeEach
    public void setUpConnection() {
        httpClient = new OkHttpClientBuilder().isForTest(true).build();
    }

    @SneakyThrows
    @BeforeEach
    public void setUpMockWebService() {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @SneakyThrows
    @AfterEach
    public void closeMockWebServer() {
        mockWebServer.close();
    }

    protected void setUpMockResponse(final int responseCode) {
        setUpMockResponse(responseCode, (SerializedResource) null);
    }

    @SneakyThrows
    protected void setUpMockResponse(final int responseCode, final SerializedResource responseBodyResource) {
        if (responseBodyResource == null) {
            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(responseCode)
                    .build());
            return;
        }

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(responseCode)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .body(new Buffer().write(responseBodyResource.toBytes()))
                .build());
    }

    protected String getMockWebServerUrl() {
        return String.format("http://%s:%d", mockWebServer.getHostName(), mockWebServer.getPort());
    }
}
