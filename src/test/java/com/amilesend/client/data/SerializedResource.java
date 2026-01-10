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
package com.amilesend.client.data;

import lombok.RequiredArgsConstructor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

/** Describes a resource that is used for functional unit testing to load data from the test jar. */
@RequiredArgsConstructor
public class SerializedResource {
    /** The resource path URI .*/
    private final String resourcePath;

    /**
     * Gets the resource as an {@code InputStream}.
     *
     * @return the input stream
     */
    public InputStream getResource() {
        return new BufferedInputStream(this.getClass().getResourceAsStream(resourcePath));
    }

    /**
     * Gets the resource as a byte array.
     *
     * @return the resource data
     * @throws IOException if an error occurred while reading the resource
     */
    public byte[] toBytes() throws IOException {
        try (final InputStream is = getResource()) {
            return is.readAllBytes();
        }
    }

    /**
     * Gets the resource as a gzip-compressed byte array.
     *
     * @return the compressed resource data
     * @throws IOException if an error occurred while reading the resource
     */
    public byte[] toGzipCompressedBytes() throws IOException {
        final byte[] uncompressed = getResource().readAllBytes();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length);
        try(final GZIPOutputStream gos = new GZIPOutputStream(baos)) {
            gos.write(uncompressed);
            gos.flush();
        } finally {
            baos.close();
        }

        return baos.toByteArray();
    }
}
