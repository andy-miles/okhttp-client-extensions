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

import com.amilesend.client.util.StringUtils;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProgressReportingRequestBodyTest {
    private static final long TOTAL_BYTES = 100L;

    @Mock
    private RequestBody mockDelegate;
    @Mock
    private TransferProgressCallback mockCallback;

    private ProgressReportingRequestBody requestBodyUnderTest;

    @BeforeEach
    public void setUp() {
        requestBodyUnderTest = new ProgressReportingRequestBody(mockDelegate, mockCallback, TOTAL_BYTES);
    }

    ////////////////
    // contentType
    ////////////////

    @Test
    public void contentType_withDelegate_shouldReturnContentType() {
        final MediaType expected = MediaType.parse("text/csv");
        when(mockDelegate.contentType()).thenReturn(expected);

        final MediaType actual = requestBodyUnderTest.contentType();

        assertEquals(expected, actual);
    }

    ////////////
    // writeTo
    ////////////

    @Test
    @SneakyThrows
    public void writeTo_withBufferedSink_shouldWriteToDelegate() {
        final BufferedSink expected = mock(BufferedSink.class);

        try (final MockedStatic<Okio> okioMockedStatic = mockStatic(Okio.class);
             final MockedConstruction<TrackingSink> sinkMockedCtor = mockConstruction(TrackingSink.class)) {
            okioMockedStatic.when(() -> Okio.buffer(any(Sink.class))).thenReturn(expected);

            requestBodyUnderTest.writeTo(mock(BufferedSink.class));

            assertAll(
                    () -> verify(mockDelegate).writeTo(eq(expected)),
                    () -> verify(expected).flush());
        }
    }

    @Test
    @SneakyThrows
    public void writeTo_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(mockDelegate).writeTo(any(BufferedSink.class));
        final BufferedSink mockWrappedSink = mock(BufferedSink.class);

        try (final MockedStatic<Okio> okioMockedStatic = mockStatic(Okio.class);
             final MockedConstruction<TrackingSink> sinkMockedCtor = mockConstruction(TrackingSink.class)) {
            okioMockedStatic.when(() -> Okio.buffer(any(Sink.class))).thenReturn(mockWrappedSink);

            assertAll(
                    () -> assertThrows(IOException.class, () -> requestBodyUnderTest.writeTo(mock(BufferedSink.class))),
                    () -> verifyNoInteractions(mockWrappedSink));
        }
    }

    @Test
    public void writeTo_withNullSink_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> requestBodyUnderTest.writeTo(null));
    }

    /////////////////////
    // multiPartBuilder
    /////////////////////

    @Test
    @SneakyThrows
    public void multiPartBuilder_withValidParameters_shouldReturnRequestBody() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            final RequestBody actual = ProgressReportingRequestBody.multiPartBuilder()
                    .file(newValidMockedPath())
                    .fieldName("upload")
                    .contentType("text/csv")
                    .callback(mockCallback)
                    .build();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, actual),
                    () -> assertTrue(actual.contentType()
                            .toString()
                            .startsWith("multipart/form-data; boundary=")));
        }
    }

    @Test
    @SneakyThrows
    public void multiPartBuilder_withNoCallbackDefined_shouldSetDefault() {
        final LogProgressCallback.LogProgressCallbackBuilder mockBuilder = newMockCallbackBuilder();

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<LogProgressCallback> callbackMockedStatic = mockStatic(LogProgressCallback.class)) {
            // Setup Files
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);
            // Setup callback builder
            callbackMockedStatic.when(() -> LogProgressCallback.formatPrefix(anyString(), anyString()))
                    .thenReturn("prefix");
            callbackMockedStatic.when(() -> LogProgressCallback.builder()).thenReturn(mockBuilder);


            final ProgressReportingRequestBody actual = ProgressReportingRequestBody.multiPartBuilder()
                    .file(newValidMockedPath())
                    .fieldName("upload")
                    .contentType("text/csv")
                    .destination("destination")
                    .build();

            assertInstanceOf(LogProgressCallback.class, actual.getCallback());
        }
    }

    @Test
    @SneakyThrows
    public void multiPartBuilder_withBlankContentType_shouldSetDefault() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<TransferFileUtil> utilMockedStatic = mockStatic(TransferFileUtil.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);
            utilMockedStatic.when(() -> TransferFileUtil.fetchMimeTypeFromFile(any(Path.class)))
                    .thenReturn("text/csv");

            final RequestBody actual = ProgressReportingRequestBody.multiPartBuilder()
                    .file(newValidMockedPath())
                    .fieldName("upload")
                    .contentType(StringUtils.EMPTY)
                    .callback(mockCallback)
                    .build();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, actual),
                    () -> assertTrue(actual.contentType()
                            .toString()
                            .startsWith("multipart/form-data; boundary=")));
        }
    }

    @Test
    @SneakyThrows
    public void multiPartBuilder_withIOException_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenThrow(new IOException("Exception")); // <--

            assertThrows(IOException.class, () -> ProgressReportingRequestBody.multiPartBuilder()
                    .file(mock(Path.class))
                    .fieldName("upload")
                    .contentType("text/csv")
                    .callback(mockCallback)
                    .build());
        }
    }

    @Test
    public void multiPartBuilder_withEmptyFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(0L); // <--

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.multiPartBuilder()
                            .file(mock(Path.class))
                            .fieldName("upload")
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void multiPartBuilder_withUnreadableFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(false); // <--
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.multiPartBuilder()
                            .file(mock(Path.class))
                            .fieldName("upload")
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void multiPartBuilder_withNonRegularFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(false); // <--
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.multiPartBuilder()
                            .file(mock(Path.class))
                            .fieldName("upload")
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void multiPartBuilder_withNonExistentFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false); // <--
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.multiPartBuilder()
                            .file(mock(Path.class))
                            .fieldName("upload")
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void multiPartBuilder_withNullFile_shouldThrowException() {
        assertThrows(NullPointerException.class,
                () -> ProgressReportingRequestBody.multiPartBuilder()
                        .file(null) // <--
                        .fieldName("upload")
                        .contentType("text/csv")
                        .callback(mockCallback)
                        .build());
    }

    @Test
    public void multiPartBuilder_withBlankFieldName_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> ProgressReportingRequestBody.multiPartBuilder()
                                .file(mock(Path.class))
                                .fieldName(null) // <--
                                .contentType("text/csv")
                                .callback(mockCallback)
                                .build()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ProgressReportingRequestBody.multiPartBuilder()
                                .file(mock(Path.class))
                                .fieldName(StringUtils.EMPTY) // <--
                                .contentType("text/csv")
                                .callback(mockCallback)
                                .build()));
    }

    ////////////
    // builder
    ////////////

    @Test
    @SneakyThrows
    public void builder_withValidParameters_shouldReturnRequestBody() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            final RequestBody actual = ProgressReportingRequestBody.builder()
                    .file(newValidMockedPath())
                    .contentType("text/csv")
                    .callback(mockCallback)
                    .build();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, actual),
                    () -> assertEquals("text/csv", actual.contentType().toString()));
        }
    }

    @Test
    @SneakyThrows
    public void builder_withNoCallbackDefined_shouldSetDefault() {
        final LogProgressCallback.LogProgressCallbackBuilder mockBuilder = newMockCallbackBuilder();

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<LogProgressCallback> callbackMockedStatic = mockStatic(LogProgressCallback.class)) {
            // Setup Files
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);
            // Setup callback builder
            callbackMockedStatic.when(() -> LogProgressCallback.formatPrefix(anyString(), anyString()))
                    .thenReturn("prefix");
            callbackMockedStatic.when(() -> LogProgressCallback.builder()).thenReturn(mockBuilder);


            final ProgressReportingRequestBody actual = ProgressReportingRequestBody.builder()
                    .file(newValidMockedPath())
                    .contentType("text/csv")
                    .destination("destination")
                    .build();

            assertInstanceOf(LogProgressCallback.class, actual.getCallback());
        }
    }

    @Test
    @SneakyThrows
    public void builder_withBlankContentType_shouldSetDefault() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<TransferFileUtil> utilMockedStatic = mockStatic(TransferFileUtil.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);
            utilMockedStatic.when(() -> TransferFileUtil.fetchMimeTypeFromFile(any(Path.class)))
                    .thenReturn("text/csv");

            final RequestBody actual = ProgressReportingRequestBody.builder()
                    .file(newValidMockedPath())
                    .contentType(StringUtils.EMPTY)
                    .callback(mockCallback)
                    .build();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, actual),
                    () -> assertEquals("text/csv", actual.contentType().toString()));
        }
    }

    @Test
    @SneakyThrows
    public void builder_withIOException_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenThrow(new IOException("Exception")); // <--

            assertThrows(IOException.class, () -> ProgressReportingRequestBody.builder()
                    .file(mock(Path.class))
                    .contentType("text/csv")
                    .callback(mockCallback)
                    .build());
        }
    }

    @Test
    public void builder_withEmptyFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(0L); // <--

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.builder()
                            .file(mock(Path.class))
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void builder_withUnreadableFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(false); // <--
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.builder()
                            .file(mock(Path.class))
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void builder_withNonRegularFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(false); // <--
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.builder()
                            .file(mock(Path.class))
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void builder_withNonExistentFile_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false); // <--
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isRegularFile(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.size(any(Path.class))).thenReturn(100L);

            assertThrows(IllegalArgumentException.class,
                    () -> ProgressReportingRequestBody.builder()
                            .file(mock(Path.class))
                            .contentType("text/csv")
                            .callback(mockCallback)
                            .build());
        }
    }

    @Test
    public void builder_withNullFile_shouldThrowException() {
        assertThrows(NullPointerException.class,
                () -> ProgressReportingRequestBody.builder()
                        .file(null) // <--
                        .contentType("text/csv")
                        .callback(mockCallback)
                        .build());
    }

    private LogProgressCallback.LogProgressCallbackBuilder newMockCallbackBuilder() {
        final LogProgressCallback mockCallback = mock(LogProgressCallback.class);

        final LogProgressCallback.LogProgressCallbackBuilder mockBuilder =
                mock(LogProgressCallback.LogProgressCallbackBuilder.class);
        when(mockBuilder.prefix(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.transferType(any(LogProgressCallback.TransferType.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockCallback);

        return mockBuilder;
    }

    private Path newValidMockedPath() {
        final Path mockPathFileName = mock(Path.class);
        lenient().when(mockPathFileName.toString()).thenReturn("Filename.csv");
        final File mockFile = mock(File.class);
        final Path mockPath = mock(Path.class);
        lenient().when(mockPath.getFileName()).thenReturn(mockPathFileName);
        when(mockPath.toFile()).thenReturn(mockFile);

        return mockPath;
    }
}
