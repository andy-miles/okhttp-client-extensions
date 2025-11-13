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
import lombok.SneakyThrows;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FixedDelayRetryStrategyTest {
    @Mock
    private Retriable mockCall;
    private FixedDelayRetryStrategy strategyUnderTest = spy(FixedDelayRetryStrategy.builder().delayMs(1L).build());

    @Test
    @SneakyThrows
    public void invoke_withSuccessfulFirstTry_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        when(mockCall.call()).thenReturn(mockResponse);
        doNothing().when(strategyUnderTest).validateResponseCode(any(Response.class));
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(1)
                .response(mockResponse)
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertTrue(actual.isSuccess()),
                () -> verify(mockCall).call());
    }

    @Test
    @SneakyThrows
    public void invoke_withSuccessAfterRetries_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        when(mockCall.call()).thenReturn(mockResponse);
        final Exception first = new ResponseException("Exception1");
        final Exception second = new ResponseException("Exception2");
        doThrow(first)
                .doThrow(second)
                .doNothing()
                .when(strategyUnderTest).validateResponseCode(any(Response.class));
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(3)
                .exceptions(List.of(first, second))
                .response(mockResponse)
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertTrue(actual.isSuccess()),
                () -> verify(mockCall, times(3)).call());
    }

    @Test
    @SneakyThrows
    public void invoke_withNoSuccessfulTries_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        when(mockCall.call()).thenReturn(mockResponse);
        final Exception first = new ResponseException("Exception1");
        final Exception second = new ResponseException("Exception2");
        final Exception third = new ResponseException("Exception3");
        doThrow(first)
                .doThrow(second)
                .doThrow(third)
                .when(strategyUnderTest).validateResponseCode(any(Response.class));
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(3)
                .exceptions(List.of(first, second, third))
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertFalse(actual.isSuccess()),
                () -> verify(mockCall, times(3)).call());
    }

    @Test
    @SneakyThrows
    public void invoke_withRequestException_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        when(mockCall.call()).thenReturn(mockResponse);
        final Exception thrown = new RequestException("Exception");
        doThrow(thrown).when(strategyUnderTest).validateResponseCode(any(Response.class));
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(1)
                .exceptions(Collections.singletonList(thrown))
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertFalse(actual.isSuccess()),
                () -> verify(mockCall).call());
    }

    @Test
    @SneakyThrows
    public void invoke_withThrottledTries_shouldReturnResponse() {
        final Response mockResponse = mock(Response.class);
        when(mockCall.call()).thenReturn(mockResponse);
        final Exception throttledException = new ThrottledException("Exception", 0L);
        doThrow(throttledException)
                .doNothing()
                .when(strategyUnderTest).validateResponseCode(mockResponse);
        final RetriableCallResponse expected = RetriableCallResponse.builder()
                .attempts(2)
                .exceptions(Collections.singletonList(throttledException))
                .response(mockResponse)
                .build();

        final RetriableCallResponse actual = strategyUnderTest.invoke(mockCall);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertTrue(actual.isSuccess()),
                () -> verify(mockCall, times(2)).call());
    }

    @Test
    @SneakyThrows
    public void invoke_withNullRetriable_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> strategyUnderTest.invoke(null));
    }
}
