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

import com.google.common.net.HttpHeaders;
import okhttp3.Headers;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.amilesend.client.connection.Connection.JSON_CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConnectionTest extends ConnectionTestBase {
    private static final String BASE_URL = "https://base.com/";
    private static final String USER_AGENT = "ConnectionTest/1.0";

    /////////
    // ctor
    /////////

    @Test
    public void ctor_withInvalidInput_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new DefaultConnectionBuilder()
                                .httpClient(null)
                                .gsonFactory(mockGsonFactory)
                                .authManager(mockAuthManager)
                                .baseUrl(BASE_URL)
                                .userAgent(USER_AGENT)
                                .build()),
                () -> assertThrows(NullPointerException.class,
                        () -> new DefaultConnectionBuilder()
                                .httpClient(mockHttpClient)
                                .gsonFactory(null)
                                .authManager(mockAuthManager)
                                .baseUrl(BASE_URL)
                                .userAgent(USER_AGENT)
                                .build()));
    }

    //////////////////////
    // newRequestBuilder
    //////////////////////

    @Test
    public void newRequestBuilder_withGzipEnabled_shouldDefineHeaders() {
        final Request.Builder expected = new Request.Builder();
        when(mockAuthManager.addAuthentication(any(Request.Builder.class))).thenReturn(expected);

        final Request.Builder actual = connectionUnderTest.newRequestBuilder();

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verifyHeaders(JSON_CONTENT_TYPE, true));
    }

    @Test
    public void newRequestBuilder_withGzipDisabled_shouldDefineHeaders() {
        connectionUnderTest = spy(new DefaultConnectionBuilder()
                .httpClient(mockHttpClient)
                .gsonFactory(mockGsonFactory)
                .authManager(mockAuthManager)
                .baseUrl("http://baseurl")
                .userAgent(USER_AGENT)
                .isGzipContentEncodingEnabled(false)
                .build());
        final Request.Builder expected = new Request.Builder();
        when(mockAuthManager.addAuthentication(any(Request.Builder.class))).thenReturn(expected);

        final Request.Builder actual = connectionUnderTest.newRequestBuilder();

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verifyHeaders(JSON_CONTENT_TYPE, false));

    }

    private void verifyHeaders(final String acceptType, boolean isAcceptEncodingValidated) {
        final ArgumentCaptor<Request.Builder> requestBuilderCaptor = ArgumentCaptor.forClass(Request.Builder.class);
        verify(mockAuthManager).addAuthentication(requestBuilderCaptor.capture());
        final Headers headers = requestBuilderCaptor.getValue().url("Http://someurl").build().headers();
        assertAll(
                () -> assertEquals(USER_AGENT, headers.get(HttpHeaders.USER_AGENT)),
                () -> assertEquals(acceptType, headers.get(HttpHeaders.ACCEPT)),
                () -> {
                    if (isAcceptEncodingValidated) {
                        assertEquals("gzip", headers.get(HttpHeaders.ACCEPT_ENCODING));
                    }
                });
    }
}
