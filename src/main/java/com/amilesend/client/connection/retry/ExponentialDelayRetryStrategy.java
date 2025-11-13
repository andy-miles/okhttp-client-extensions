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

import com.amilesend.client.connection.ResponseException;
import com.amilesend.client.connection.ThrottledException;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An exponential-delay-based retry strategy.
 *
 * @see RetryStrategy
 */
@Builder
@Slf4j
public class ExponentialDelayRetryStrategy implements RetryStrategy {
    /** The maximum number of attempts to make. Default: 3 */
    @Builder.Default
    private final int maxAttempts = 3;
    /** The maximum amount of jitter in milliseconds to apply per retry. Default: 100 */
    @Builder.Default
    private final long maxJitterMs = 100L;
    /** The base delay in milliseconds to apply per retry. Default: 1000 */
    @Builder.Default
    private final long baseDelayMs = 500L;
    /** The maximum total delay across all retries. Default: 5000 */
    @Builder.Default
    private final long maxTotalDelayMs = 3000L;

    @Override
    public RetriableCallResponse invoke(@NonNull final Retriable retriable) {
        int attempts = 0;
        final List<Exception> exceptions = new ArrayList<>(maxAttempts);
        long totalTimeMs = 0L;

        do {
            try {
                ++attempts;
                final Response response = retriable.call();
                validateResponseCode(response);
                return RetriableCallResponse.builder()
                        .response(response)
                        .exceptions(exceptions)
                        .attempts(attempts)
                        .build();
            } catch (final IOException | ThrottledException | ResponseException ex) {
                exceptions.add(ex);
                final long delay = calculateDelay(attempts, ex);
                totalTimeMs += delay;

                if (attempts >= maxAttempts | totalTimeMs >= maxTotalDelayMs) {
                    return RetriableCallResponse.builder()
                            .attempts(attempts)
                            .exceptions(exceptions)
                            .build();
                }

                try {
                    log.debug("Delaying next retry by {} ms", delay);
                    Thread.sleep(delay);
                } catch (final InterruptedException iex) {
                    exceptions.add(ex);
                    Thread.currentThread().interrupt();
                    return RetriableCallResponse.builder()
                            .attempts(attempts)
                            .exceptions(exceptions)
                            .build();
                }
            } catch (final Exception ex) {
                exceptions.add(ex);
                return RetriableCallResponse.builder()
                        .attempts(attempts)
                        .exceptions(exceptions)
                        .build();
            }
        } while (true);
    }

    protected long calculateDelay(final int attemptNum, final Exception thrown) {
        final long jitter = (long)(Math.random() * maxJitterMs);
        long delayWithoutJitter = (long) Math.pow(baseDelayMs * 2, attemptNum);

        if (ThrottledException.class.isInstance(thrown)) {
            final long delayFromHeader = ((ThrottledException) thrown).getRetryAfterSeconds() * 1000L;
            delayWithoutJitter = Math.max(delayWithoutJitter, delayFromHeader);
        }

        return delayWithoutJitter + jitter;
    }
}
