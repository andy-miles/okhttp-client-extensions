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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;

import static com.amilesend.client.connection.file.TransferFileUtil.fetchMimeTypeFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransferFileUtilTest {
    private static final String CONTENT_TYPE = "application/zip";

    /////////
    // Path
    /////////

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withValidPath_shouldReturnMimeType() {
        final Path mockFilePath = setUpFilePathMock();
        final String actual = fetchMimeTypeFromFile(mockFilePath);
        assertEquals(CONTENT_TYPE, actual);
    }

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withNoURLInputStreamToCloseFromPath_shouldReturnMimeType() {
        final Path mockFilePath = setUpFilePathMockWithNoInputStreamToClose();
        final String actual = fetchMimeTypeFromFile(mockFilePath);
        assertEquals(CONTENT_TYPE, actual);
    }

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withPathAndIOException_shouldThrowException() {
        final Path mockFilePath = setUpFilePathMock(new IOException("Exception"));
        assertThrows(IOException.class, () -> fetchMimeTypeFromFile(mockFilePath));
    }

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withNullPath_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> fetchMimeTypeFromFile((Path) null));
    }

    @SneakyThrows
    private Path setUpFilePathMock() {
        final URLConnection mockConnection = mock(URLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockConnection.getContentType()).thenReturn(CONTENT_TYPE);

        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenReturn(mockConnection);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final Path mockFilePath = mock(Path.class);
        when(mockFilePath.toUri()).thenReturn(mockURI);

        return mockFilePath;
    }

    @SneakyThrows
    private Path setUpFilePathMockWithNoInputStreamToClose() {
        final URLConnection mockConnection = mock(URLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(null);
        when(mockConnection.getContentType()).thenReturn(CONTENT_TYPE);

        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenReturn(mockConnection);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final Path mockFilePath = mock(Path.class);
        when(mockFilePath.toUri()).thenReturn(mockURI);

        return mockFilePath;
    }

    @SneakyThrows
    private Path setUpFilePathMock(final IOException exceptionToThrow) {
        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenThrow(exceptionToThrow);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final Path mockFilePath = mock(Path.class);
        when(mockFilePath.toUri()).thenReturn(mockURI);

        return mockFilePath;
    }

    /////////
    // File
    /////////

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withValidFile_shouldReturnMimeType() {
        final File mockFile = setUpFileMock();
        final String actual = fetchMimeTypeFromFile(mockFile);
        assertEquals(CONTENT_TYPE, actual);
    }

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withNoURLInputStreamToClose_shouldReturnMimeType() {
        final File mockFile = setUpFileMockWithNoInputStreamToClose();
        final String actual = fetchMimeTypeFromFile(mockFile);
        assertEquals(CONTENT_TYPE, actual);
    }

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withIOException_shouldThrowException() {
        final File mockFile = setUpFileMock(new IOException("Exception"));
        assertThrows(IOException.class, () -> fetchMimeTypeFromFile(mockFile));
    }

    @SneakyThrows
    @Test
    public void fetchMimeTypeFromFile_withNullFile_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> fetchMimeTypeFromFile((File) null));
    }

    @SneakyThrows
    private File setUpFileMock() {
        final URLConnection mockConnection = mock(URLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockConnection.getContentType()).thenReturn(CONTENT_TYPE);

        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenReturn(mockConnection);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final File mockFile = mock(File.class);
        when(mockFile.toURI()).thenReturn(mockURI);

        return mockFile;
    }

    @SneakyThrows
    private File setUpFileMockWithNoInputStreamToClose() {
        final URLConnection mockConnection = mock(URLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(null);
        when(mockConnection.getContentType()).thenReturn(CONTENT_TYPE);

        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenReturn(mockConnection);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final File mockFile = mock(File.class);
        when(mockFile.toURI()).thenReturn(mockURI);

        return mockFile;
    }

    @SneakyThrows
    private File setUpFileMock(final IOException exceptionToThrow) {
        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenThrow(exceptionToThrow);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final File mockFile = mock(File.class);
        when(mockFile.toURI()).thenReturn(mockURI);

        return mockFile;
    }
}
