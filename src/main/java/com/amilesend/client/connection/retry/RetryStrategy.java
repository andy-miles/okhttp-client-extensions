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
package com.amilesend.client.connection.retry;

import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.client.connection.ThrottledException;
import com.amilesend.client.util.StringUtils;
import okhttp3.Response;

/**
 * Defines the interface for a retry strategy that enables different approaches to determine how and when
 * to retry an invocation.
 */
public interface RetryStrategy {
    /** The HTTP header for the number of seconds to wait for a retry when an invocation is throttled. */
    String THROTTLED_RETRY_AFTER_HEADER = "Retry-After";
    /** The default amount of seconds to wait for a throttled response. */
    Long DEFAULT_RETRY_AFTER_SECONDS = Long.valueOf(1L);
    /** The throttled HTTP response code. */
    int THROTTLED_RESPONSE_CODE = 429;

    /**
     * Executes the strategy to invoke the {@link RetriableCallResponse} call.
     *
     * @param retriable the call to invoke
     * @return the response
     */
    RetriableCallResponse invoke(Retriable retriable);

    /**
     * Validates the response code for a response.
     *
     * @param response the response to evaluate
     * @throws ThrottledException if a response was throttled
     * @throws RequestException if a response contains a 400-based response code value
     * @throws ResponseException if a response contains a non 400-based response code value
     */
    default void validateResponseCode(final Response response) {
        if (response.isSuccessful()) {
            return;
        }

        final int code = response.code();
        final boolean isRequestError = (code / 100 == 4);

        if (!isRequestError) {
            throw new ResponseException("Unsuccessful response (" + code + "): " + response);
        }

        if (code == THROTTLED_RESPONSE_CODE) {
            final Long retryAfterSeconds = extractRetryAfterHeaderValue(response);
            final String msg = "Request throttled. Retry after " + retryAfterSeconds + " seconds";
            throw new ThrottledException(msg, retryAfterSeconds);
        }

        throw new RequestException("Error with request (" + code + "): " + response);
    }

    /**
     * Extracts the defined throttle retry value from the response header, or the default if none is defined.
     *
     * @param response the response
     * @return the amount of time in seconds to wait before the next retry
     */
    default Long extractRetryAfterHeaderValue(final Response response) {
        final String retryAfterHeaderValue = response.header(THROTTLED_RETRY_AFTER_HEADER);
        return StringUtils.isNotBlank(retryAfterHeaderValue)
                ? Long.valueOf(retryAfterHeaderValue)
                : DEFAULT_RETRY_AFTER_SECONDS;
    }
}
