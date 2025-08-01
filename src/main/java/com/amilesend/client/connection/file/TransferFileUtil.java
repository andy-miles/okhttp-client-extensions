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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;

/** Shared utility class used by transfer logic. */
@UtilityClass
public class TransferFileUtil {
    /**
     * Fetches the mime type for the given {@code filePath}. This is used to specify the content-type for requests
     * and replies.
     *
     * @param filePath the file
     * @return the string formatted mime type
     * @throws IOException if an error occurred while reading the file
     */
    @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
    public static String fetchMimeTypeFromFile(@NonNull final Path filePath) throws IOException {
        final URLConnection urlConnection = filePath.toUri().toURL().openConnection();
        try {
            return urlConnection.getContentType();
        } finally {
            final InputStream connectionStream = urlConnection.getInputStream();
            if (connectionStream != null) {
                connectionStream.close();
            }
        }
    }

    /**
     * Fetches the mime type for the given {@code file}. This is used to specify the content-type for requests
     * and replies.
     *
     * @param file the file
     * @return the string formatted mime type
     * @throws IOException if an error occurred while reading the file
     */
    @SuppressFBWarnings("URLCONNECTION_SSRF_FD")
    public static String fetchMimeTypeFromFile(@NonNull final File file) throws IOException {
        final URLConnection urlConnection = file.toURI().toURL().openConnection();
        try {
            return urlConnection.getContentType();
        } finally {
            final InputStream connectionStream = urlConnection.getInputStream();
            if (connectionStream != null) {
                connectionStream.close();
            }
        }
    }
}
