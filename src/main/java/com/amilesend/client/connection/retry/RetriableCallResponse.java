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
package com.amilesend.client.connection.retry;

import lombok.Builder;
import lombok.Data;
import okhttp3.Response;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** The response from the invocation of a {@link com.amilesend.client.connection.retry.RetryStrategy}. */
@Builder
@Data
public class RetriableCallResponse {
    /** The number of attempts that were made. */
    private final int attempts;
    /** The list of exceptions from any failures. */
    @Builder.Default
    private final List<Exception> exceptions = Collections.emptyList();
    /** The response for the last successful invocation.*/
    private final Response response;

    /**
     * Indicates if the response is successful or not.
     *
     * @return {@code true} if successful; else, {@code false}
     */
    public boolean isSuccess() {
        return Objects.nonNull(response);
    }
}
