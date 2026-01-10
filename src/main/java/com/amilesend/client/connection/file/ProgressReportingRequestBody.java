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
package com.amilesend.client.connection.file;

import com.amilesend.client.util.StringUtils;
import com.amilesend.client.util.Validate;
import com.amilesend.client.util.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static com.amilesend.client.connection.file.TransferFileUtil.fetchMimeTypeFromFile;

/**
 * A customized implementation of {@link RequestBody} that wraps an existing request body and uses
 * a custom sink track transfer progress.
 *
 * @see RequestBody
 * @see TransferProgressCallback
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ProgressReportingRequestBody extends RequestBody {
    /** The request body that is wrapped. */
    @NonNull
    private final RequestBody delegate;
    /** The callback for transfer progress notifications. */
    @NonNull
    @VisibleForTesting
    @Getter(AccessLevel.PACKAGE)
    private final TransferProgressCallback callback;
    /** The total number of bytes to transfer. */
    private final long totalBytes;

    /**
     * Returns a new {@link Builder} instance.
     *
     * @return the builder
     * @see Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a new {@link MultiPartBuilder} instance.
     *
     * @return the builder
     * @see MultiPartBuilder
     */
    public static MultiPartBuilder multiPartBuilder() {
        return new MultiPartBuilder();
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public void writeTo(@NonNull final BufferedSink sink) throws IOException {
        final BufferedSink bufferedSink = Okio.buffer(new TrackingSink(sink, callback, totalBytes));
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    /**
     * Helper method to format the logging prefix to use.
     *
     * @param source the source
     * @param destination the destination
     * @return the logging prefix
     */
    private static String formatPrefix(final String source, final String destination) {
        Validate.notBlank(source, "source must not be blank");
        Validate.notBlank(destination, "destination must not be blank");

        return new StringBuilder("[")
                .append(source)
                .append(" -> ")
                .append(destination)
                .append("] ")
                .toString();
    }

    private static void validateFile(final Path file) throws IOException {
        Validate.notNull(file, "file must not be null");
        Validate.isTrue(Files.exists(file), "file must exist");
        Validate.isTrue(Files.isRegularFile(file), "file must be a regular file");
        Validate.isTrue(Files.isReadable(file), "file must be readable");
        Validate.isTrue(Files.size(file) > 0L, "file size must not be empty");
    }

    private static MediaType parseContentType(final String contentType) {
        return MediaType.parse(contentType);
    }

    /** The builder used to construct new {@link ProgressReportingRequestBody} instances for multipart uploads. */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MultiPartBuilder {
        /** The request form field name for the upload. */
        private String fieldName = "upload";
        /** The source files to read and upload. */
        private Path file;
        /** The mime type of the file. */
        private String contentType;
        /** The destination descriptor for reporting (i.e., the name of the service). */
        private String destination;
        /** The {@link TransferProgressCallback}. */
        private TransferProgressCallback callback;

        /**
         * The file to upload.
         *
         * @param file the path of the file to upload
         * @return the builder
         */
        public MultiPartBuilder file(final Path file) {
            this.file = file;
            return this;
        }

        /**
         * The attribute field name for the form data in the request. Default is {@code upload}.
         *
         * @param fieldName the field name
         * @return the builder
         */
        public MultiPartBuilder fieldName(final String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        /**
         * The content type. The default is {@code text/csv}.
         *
         * @param contentType the content type
         * @return the builder
         */
        public MultiPartBuilder contentType(final String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * The destination descriptor (i.e., the name of the service).
         *
         * @param destination the destination
         * @return the builder
         */
        public MultiPartBuilder destination(final String destination) {
            this.destination = destination;
            return this;
        }

        /**
         * The callback listener. Default is a {@link LogProgressCallback}.
         *
         * @param callback the listener
         * @return the builder
         */
        public MultiPartBuilder callback(final TransferProgressCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Builds a new {@link RequestBody} that tracks the transfer progress for multipart form-data transfer.
         *
         * @return the request body
         * @throws IOException if an error occurred while accessing the file to read
         */
        public ProgressReportingRequestBody build() throws IOException {
            Validate.notBlank(fieldName, "fieldName must not be blank");
            validateFile(file);

            MediaType mediaType = Optional.ofNullable(contentType)
                    .filter(StringUtils::isNotBlank)
                    .map(ProgressReportingRequestBody::parseContentType)
                    .orElse(null);
            // Not included in the optional chain above in order to propagate any IOExceptions thrown
            if (Objects.isNull(mediaType)) {
                mediaType = parseContentType(fetchMimeTypeFromFile(file));
            }

            callback = Optional.ofNullable(callback)
                    .orElseGet(() -> LogProgressCallback.builder()
                            .prefix(formatPrefix(file.getFileName().toString(), destination))
                            .transferType(LogProgressCallback.TransferType.UPLOAD)
                            .build());

            final RequestBody multipartBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(fieldName,
                            file.getFileName().toString(),
                            RequestBody.create(file.toFile(), mediaType))
                    .build();

            return new ProgressReportingRequestBody(multipartBody, callback, Files.size(file));
        }
    }

    /** The builder used to construct new {@link ProgressReportingRequestBody} instances for uploads. */
    public static class Builder {
        /** The source files to read and upload. */
        private Path file;
        /** The mime type of the file. */
        private String contentType;
        /** The destination descriptor for reporting (i.e., the name of the service). */
        private String destination;
        /** The {@link TransferProgressCallback}. */
        private TransferProgressCallback callback;

        /**
         * The file to upload.
         *
         * @param file the path of the file to upload
         * @return the builder
         */
        public Builder file(final Path file) {
            this.file = file;
            return this;
        }

        /**
         * The content type. The default is {@code text/csv}.
         *
         * @param contentType the content type
         * @return the builder
         */
        public Builder contentType(final String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * The destination descriptor (i.e., the name of the service).
         *
         * @param destination the destination
         * @return the builder
         */
        public Builder destination(final String destination) {
            this.destination = destination;
            return this;
        }

        /**
         * The callback listener. Default is a {@link LogProgressCallback}.
         *
         * @param callback the listener
         * @return the builder
         */
        public Builder callback(final TransferProgressCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * Builds a new {@link RequestBody} that tracks the transfer progress for multipart form-data transfer.
         *
         * @return the request body
         * @throws IOException if an error occurred while accessing the file to read
         */
        public ProgressReportingRequestBody build() throws IOException {
            validateFile(file);

            MediaType mediaType = Optional.ofNullable(contentType)
                    .filter(StringUtils::isNotBlank)
                    .map(ProgressReportingRequestBody::parseContentType)
                    .orElse(null);
            // Not included in the optional chain above in order to propagate any IOExceptions thrown
            if (Objects.isNull(mediaType)) {
                mediaType = parseContentType(fetchMimeTypeFromFile(file));
            }

            callback = Optional.ofNullable(callback)
                    .orElseGet(() -> LogProgressCallback.builder()
                            .prefix(formatPrefix(file.getFileName().toString(), destination))
                            .transferType(LogProgressCallback.TransferType.UPLOAD)
                            .build());

            final RequestBody requestBody = RequestBody.create(file.toFile(), mediaType);

            return new ProgressReportingRequestBody(requestBody, callback, Files.size(file));
        }
    }
}
