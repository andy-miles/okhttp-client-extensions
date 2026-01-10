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
package com.amilesend.client.connection.retry;

import com.amilesend.client.connection.ResponseException;
import lombok.SneakyThrows;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NoRetryStrategyTest {
    @Mock
    private Retriable mockCall;
    @Spy
    private NoRetryStrategy strategyUnderTest;

    @Test
    @SneakyThrows
    public void invoke_withValidSuccessfulRetriable_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        when(mockCall.call()).thenReturn(mockResponse);
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(1)
                .response(mockResponse)
                .build();
        doNothing().when(strategyUnderTest).validateResponseCode(any(Response.class));

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void invoke_withExceptionFromCall_shouldReturnResponse() {
        final Exception thrownException = new IOException("Exception");
        when(mockCall.call()).thenThrow(thrownException);
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(1)
                .exceptions(Collections.singletonList(thrownException))
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void invoke_withExceptionFromResponseCodeValidation_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        doReturn(mockResponse).when(mockCall).call();
        final Exception thrownException = new ResponseException("Exception");
        doThrow(thrownException).when(strategyUnderTest).validateResponseCode(any(Response.class));
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(1)
                .exceptions(Collections.singletonList(thrownException))
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void invoke_withNullRetriable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> strategyUnderTest.invoke(null));
    }
}
