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
package com.amilesend.client.connection.retry;

import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.client.connection.ThrottledException;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RetryStrategyTest {
    @Mock
    private Response mockResponse;
    @Spy
    private TestRetryStrategy retryStrategyUnderTest;

    /////////////////////////
    // validateResponseCode
    /////////////////////////

    @Test
    public void validateResponseCode_withValidResponse_shouldDoNothing() {
        when(mockResponse.isSuccessful()).thenReturn(true);
        retryStrategyUnderTest.validateResponseCode(mockResponse);
    }

    @Test
    public void validateResponseCode_withThrottledResponse_shouldThrowThrottledException() {
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(429);
        doReturn(2L).when(retryStrategyUnderTest).extractRetryAfterHeaderValue(any(Response.class));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> retryStrategyUnderTest.validateResponseCode(mockResponse));
        assertEquals(2L, ((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void validateResponseCode_withRequestResponseCode_shouldThrowRequestException() {
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(404);

        assertThrows(RequestException.class, () -> retryStrategyUnderTest.validateResponseCode(mockResponse));
    }

    @Test
    public void validateResponseCode_withServiceErrorResponseCode_shouldThrowResponseException() {
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(503);

        assertThrows(ResponseException.class, () -> retryStrategyUnderTest.validateResponseCode(mockResponse));
    }

    /////////////////////////////////
    // extractRetryAfterHeaderValue
    /////////////////////////////////

    @Test
    public void extractRetryAfterHeaderValue_withHeaderValue_shouldReturnDefinedValue() {
        when(mockResponse.header(eq("Retry-After"))).thenReturn("3");

        final Long actual = retryStrategyUnderTest.extractRetryAfterHeaderValue(mockResponse);

        assertEquals(3L, actual);
    }

    @Test
    public void extractRetryAfterHeaderValue_withNoValueDefined_shouldReturnDefault() {
        final Long actual = retryStrategyUnderTest.extractRetryAfterHeaderValue(mockResponse);

        assertEquals(1L, actual);
    }

    @Test
    public void extractRetryAfterHeaderValue_withNonNumberValue_shouldThrowException() {
        when(mockResponse.header(eq("Retry-After"))).thenReturn("ShouldThrowException");

        assertThrows(NumberFormatException.class,
                () -> retryStrategyUnderTest.extractRetryAfterHeaderValue(mockResponse));    }

    public static class TestRetryStrategy implements RetryStrategy {
        @Override
        public RetriableCallResponse invoke(final Retriable retriable) {
            return null;
        }
    }
}
