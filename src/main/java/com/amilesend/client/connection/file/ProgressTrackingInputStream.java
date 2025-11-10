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

import com.amilesend.client.util.VisibleForTesting;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** Tracking input stream to monitor the amount of bytes read. */
public class ProgressTrackingInputStream extends InputStream {
    private static final int DEFAULT_BUFFER_SIZE = 16384;

    /** The counter keeping track of the number of bytes transferred between IO sinks. */
    private final AtomicLong bytesTransferred = new AtomicLong(0L);

    /** The delegate input stream that is being tracked. */
    @NonNull
    private final InputStream delegate;
    /**
     * The callback to notify.
     *
     * @see TransferProgressCallback
     */
    private final Optional<TransferProgressCallback> callback;
    /** The total number of bytes to transfer. */
    private final long totalBytes;

    /**
     * Creates a new {@code ProgressTrackingInputStream} object.
     *
     * @param delegate the delegate input stream
     * @param callback the callback to notify
     * @param totalBytes the total bytes to read
     */
    public ProgressTrackingInputStream(
            @NonNull final InputStream delegate,
            final TransferProgressCallback callback,
            final long totalBytes) {
        this.delegate = delegate;
        this.callback = Optional.ofNullable(callback);
        this.totalBytes = totalBytes;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public int read() throws IOException {
        try {
            final int byteValue = delegate.read();
            addProgressAndNotify(1L);
            return byteValue;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @Override
    public int read(@NonNull final byte[] b) throws IOException {
        try {
            final int bytesRead = delegate.read(b);
            addProgressAndNotify(bytesRead);
            return bytesRead;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        try {
            final int bytesRead = delegate.read(b, off, len);
            addProgressAndNotify(bytesRead);
            return bytesRead;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        try {
            final byte[] data = delegate.readAllBytes();
            addProgressAndNotify(data.length);
            return data;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @Override
    public byte[] readNBytes(final int len) throws IOException {
        try {
            final byte[] data = delegate.readNBytes(len);
            addProgressAndNotify(data.length);
            return data;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @Override
    public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
        try {
            final int bytesRead = delegate.readNBytes(b, off, len);
            addProgressAndNotify(bytesRead);
            return bytesRead;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @Override
    public long skip(final long n) throws IOException {
        try {
            final long numSkippedBytes = delegate.skip(n);
            addProgressAndNotify(numSkippedBytes);
            return numSkippedBytes;
        } catch (final IOException ex) {
            callback.ifPresent(c -> c.onFailure(ex));
            throw ex;
        }
    }

    @VisibleForTesting
    void addProgressAndNotify(final long bytesRead) {
        final long totalBytesRead = bytesTransferred.addAndGet(bytesRead);
        callback.ifPresent(c -> c.onUpdate(totalBytesRead, totalBytes));

        if (totalBytesRead >= totalBytes) {
            callback.ifPresent(c -> c.onComplete(totalBytesRead));
        }
    }
}
