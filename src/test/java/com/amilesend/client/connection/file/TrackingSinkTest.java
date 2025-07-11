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
package com.amilesend.client.connection.file;

import lombok.SneakyThrows;
import okio.Buffer;
import okio.Sink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class TrackingSinkTest {
    @Mock
    private TransferProgressCallback mockCallback;
    @Mock
    private Sink mockSink;
    private TrackingSink sinkUnderTest;

    @BeforeEach
    public void setUp() {
        sinkUnderTest = spy(new TrackingSink(mockSink, mockCallback, 100L));
    }

    @Test
    public void ctor_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new TrackingSink(null, mockCallback, 100L)),
                () -> assertThrows(NullPointerException.class,
                        () -> new TrackingSink(mockSink, null, 100L)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new TrackingSink(mockSink, mockCallback, 0L)));
    }

    @Test
    @SneakyThrows
    public void write_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(sinkUnderTest).superWrite(any(Buffer.class), anyLong());

        assertAll(
                () -> assertThrows(IOException.class, () -> sinkUnderTest.write(mock(Buffer.class), 10L)),
                () -> verify(mockCallback).onFailure(isA(IOException.class)));
    }

    @Test
    @SneakyThrows
    public void write_withBytesTransferredLessThanTotal_shouldInvokeCallback() {
        doNothing().when(sinkUnderTest).superWrite(any(Buffer.class), anyLong());
        final Buffer mockBuffer = mock(Buffer.class);

        sinkUnderTest.write(mockBuffer, 10L);

        assertAll(
                () -> verify(mockCallback).onUpdate(eq(10L), eq(100L)),
                () -> verifyNoMoreInteractions(mockCallback));
    }

    @Test
    @SneakyThrows
    public void write_withBytesTransferredEqualToTotal_shouldInvokeCallback() {
        doNothing().when(sinkUnderTest).superWrite(any(Buffer.class), anyLong());
        final Buffer mockBuffer = mock(Buffer.class);

        sinkUnderTest.write(mockBuffer, 100L);

        assertAll(
                () -> verify(mockCallback).onUpdate(eq(100L), eq(100L)),
                () -> verify(mockCallback).onComplete(eq(100L)),
                () -> verifyNoMoreInteractions(mockCallback));
    }
}
