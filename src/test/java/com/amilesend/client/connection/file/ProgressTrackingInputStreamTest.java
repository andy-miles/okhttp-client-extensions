package com.amilesend.client.connection.file;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProgressTrackingInputStreamTest {
    private static final long TOTAL_BYTES = 100L;

    @Mock
    private InputStream mockDelegate;
    @Mock
    private TransferProgressCallback mockCallback;

    private ProgressTrackingInputStream inputStreamUnderTest;

    @BeforeEach
    public void setUp() {
        inputStreamUnderTest = spy(new ProgressTrackingInputStream(mockDelegate, mockCallback, TOTAL_BYTES));
    }

    ////////////
    // close()
    ////////////

    @Test
    @SneakyThrows
    public void close_withNoException_shouldCloseDelegate() {
        inputStreamUnderTest.close();

        verify(mockDelegate).close();
    }

    @Test
    @SneakyThrows
    public void close_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(mockDelegate).close();

        assertThrows(IOException.class, () -> inputStreamUnderTest.close());
    }

    ///////////
    // read()
    ///////////

    @Test
    @SneakyThrows
    public void read_withNoException_shouldReturnByte() {
        final int expected = 8;
        when(mockDelegate.read()).thenReturn(expected);

        final int actual = inputStreamUnderTest.read();

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void read_withNoException_shouldNotifyCallback() {
        read_withNoException_shouldReturnByte();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(1L));
    }

    @Test
    @SneakyThrows
    public void read_withIOException_shouldThrowException() {
        when(mockDelegate.read()).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.read());
    }

    @Test
    @SneakyThrows
    public void read_withIOException_shouldNotifyCallback() {
        read_withIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    /////////////////
    // read(byte[])
    /////////////////

    @Test
    @SneakyThrows
    public void read_withBytes_shouldReturnNumBytesRead() {
        final int expected = 10;
        when(mockDelegate.read(any(byte[].class))).thenReturn(expected);

        final int actual = inputStreamUnderTest.read(new byte[expected]);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void read_withBytes_shouldNotifyCallback() {
        read_withBytes_shouldReturnNumBytesRead();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(10L));
    }

    @Test
    @SneakyThrows
    public void read_withBytesAndIOException_shouldThrowException() {
        when(mockDelegate.read(any(byte[].class))).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.read(new byte[10]));
    }

    @Test
    @SneakyThrows
    public void read_withBytesAndIOException_shouldNotifyCallback() {
        read_withBytesAndIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    ///////////////////////////
    // read(byte[], int, int)
    ///////////////////////////

    @Test
    @SneakyThrows
    public void read_withBytesAndOffset_shouldReturnNumBytesRead() {
        final int expected = 10;
        when(mockDelegate.read(any(byte[].class), anyInt(), anyInt())).thenReturn(expected);

        final int actual = inputStreamUnderTest.read(new byte[expected], 0, 10);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void read_withBytesAndOffset_shouldNotifyCallback() {
        read_withBytesAndOffset_shouldReturnNumBytesRead();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(10L));
    }

    @Test
    @SneakyThrows
    public void read_withBytesAndOffsetAndIOException_shouldThrowException() {
        when(mockDelegate.read(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.read(new byte[10], 0, 10));
    }

    @Test
    @SneakyThrows
    public void read_withBytesAndOffsetAndIOException_shouldNotifyCallback() {
        read_withBytesAndOffsetAndIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    ///////////////////
    // readAllBytes()
    ///////////////////

    @Test
    @SneakyThrows
    public void readAllBytes_withNoException_shouldReturnBytes() {
        final byte[] expected = new byte[] {1, 2, 3, 4};
        when(mockDelegate.readAllBytes()).thenReturn(expected);

        final byte[] actual = inputStreamUnderTest.readAllBytes();

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void readAllBytes_withNoException_shouldNotifyCallback() {
        readAllBytes_withNoException_shouldReturnBytes();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(4L));
    }

    @Test
    @SneakyThrows
    public void readAllBytes_withIOException_shouldThrowException() {
        when(mockDelegate.readAllBytes()).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.readAllBytes());
    }

    @Test
    @SneakyThrows
    public void readAllBytes_withIOException_shouldNotifyCallback() {
        readAllBytes_withIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    ////////////////////
    // readNBytes(int)
    ////////////////////

    @Test
    @SneakyThrows
    public void readNBytes_withLength_shouldReturnBytes() {
        final byte[] expected = new byte[] {1, 2, 3, 4};
        when(mockDelegate.readNBytes(anyInt())).thenReturn(expected);

        final byte[] actual = inputStreamUnderTest.readNBytes(4);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void readNBytes_withLength_shouldNotifyCallback() {
        readNBytes_withLength_shouldReturnBytes();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(4L));
    }

    @Test
    @SneakyThrows
    public void readNBytes_withIOException_shouldThrowException() {
        when(mockDelegate.readNBytes(anyInt())).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.readNBytes(4));
    }

    @Test
    @SneakyThrows
    public void readNBytes_withIOException_shouldNotifyCallback() {
        readNBytes_withIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    ///////////////////////////////
    // readNBytes(byte, int, int)
    ///////////////////////////////

    @Test
    @SneakyThrows
    public void readNBytes_withBytesAndOffset_shouldReturnNumBytesRead() {
        final int expected = 10;
        when(mockDelegate.readNBytes(any(byte[].class), anyInt(), anyInt())).thenReturn(expected);

        final int actual = inputStreamUnderTest.readNBytes(new byte[expected], 0, expected);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void readNBytes_withBytesAndOffset_shouldNotifyCallback() {
        readNBytes_withBytesAndOffset_shouldReturnNumBytesRead();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(10L));
    }

    @Test
    @SneakyThrows
    public void readNBytes_withBytesAndOffsetAndIOException_shouldThrowException() {
        when(mockDelegate.readNBytes(any(byte[].class), anyInt(), anyInt())).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.readNBytes(new byte[10], 0, 10));
    }

    @Test
    @SneakyThrows
    public void readNBytes_withBytesAndOffsetAndIOException_shouldNotifyCallback() {
        readNBytes_withBytesAndOffsetAndIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    /////////
    // skip
    /////////

    @Test
    @SneakyThrows
    public void skip_withNumBytes_shouldReturnNumBytesSkipped() {
        final long expected = 10L;
        when(mockDelegate.skip(anyLong())).thenReturn(expected);

        final long actual = inputStreamUnderTest.skip(expected);

        assertEquals(expected, actual);
    }

    @Test
    @SneakyThrows
    public void skip_withNumBytes_shouldNotifyCallback() {
        skip_withNumBytes_shouldReturnNumBytesSkipped();
        verify(inputStreamUnderTest).addProgressAndNotify(eq(10L));
    }

    @Test
    @SneakyThrows
    public void skip_withNumBytesAndIOException_shouldThrowException() {
        when(mockDelegate.skip(anyLong())).thenThrow(new IOException("Exception"));

        assertThrows(IOException.class, () -> inputStreamUnderTest.skip(10L));
    }

    @Test
    @SneakyThrows
    public void skip_withNumBytesAndIOException_shouldNotifyCallback() {
        skip_withNumBytesAndIOException_shouldThrowException();
        verify(mockCallback).onFailure(isA(IOException.class));
    }

    /////////////////////////
    // addProgressAndNotify
    /////////////////////////

    @Test
    public void addProgressAndNotify_withLessThanTotalBytesRead_shouldNotifyCallback() {
        inputStreamUnderTest.addProgressAndNotify(50L);

        verify(mockCallback).onUpdate(eq(50L), eq(TOTAL_BYTES));
    }

    @Test
    public void addProgressAndNotify_withEqualToTotalBytesRead_shouldNotifyCallback() {
        inputStreamUnderTest.addProgressAndNotify(TOTAL_BYTES);

        assertAll(
                () -> verify(mockCallback).onUpdate(eq(TOTAL_BYTES), eq(TOTAL_BYTES)),
                () -> verify(mockCallback).onComplete(eq(TOTAL_BYTES)));
    }
}
