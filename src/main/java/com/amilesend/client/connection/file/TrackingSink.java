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

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import okio.Buffer;
import okio.ForwardingSink;
import okio.Sink;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/** Tracking sink to monitor the amount of bytes transferred. */
public class TrackingSink extends ForwardingSink {
    /** The counter keeping track of the number of bytes transferred between IO sinks. */
    private final AtomicLong bytesTransferred = new AtomicLong(0L);
    /**
     * The callback to notify.
     *
     * @see TransferProgressCallback
     */
    private final TransferProgressCallback callback;
    /** The total number of bytes to transfer. */
    private final long totalBytes;

    /**
     * Creates a new {@code TrackingSink} instance.
     *
     * @param delegate the sink to track
     * @param callback the callback to notify of progress
     * @param totalBytes the total number of bytes to transfer
     */
    public TrackingSink(@NonNull final Sink delegate,
                        @NonNull final TransferProgressCallback callback,
                        final long totalBytes) {
        super(delegate);
        Validate.isTrue(totalBytes > 0L, "totalBytes must be > 0");
        this.callback = callback;
        this.totalBytes = totalBytes;
    }

    @Override
    public void write(final Buffer source, final long byteCount) throws IOException {
        try {
            superWrite(source, byteCount);
            final long processedBytes = bytesTransferred.addAndGet(byteCount);
            if (processedBytes <= totalBytes) {
                callback.onUpdate(processedBytes, totalBytes);
            }

            if (processedBytes >= totalBytes) {
                callback.onComplete(processedBytes);
            }
        } catch (final IOException ex) {
            callback.onFailure(ex);
            throw ex;
        }
    }

    @VisibleForTesting
    void superWrite(final Buffer source, final long byteCount) throws IOException {
        super.write(source, byteCount);
    }
}
