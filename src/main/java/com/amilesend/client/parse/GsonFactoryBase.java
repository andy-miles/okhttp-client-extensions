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
package com.amilesend.client.parse;

import com.amilesend.client.connection.Connection;
import com.amilesend.client.parse.strategy.AnnotationBasedExclusionStrategy;
import com.amilesend.client.parse.strategy.AnnotationBasedSerializationExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory that vends new pre-configured {@link Gson} instances.
 *
 * @param <T> The connection type that this factory is for
 */
@NoArgsConstructor
public abstract class GsonFactoryBase<T extends Connection> {
    private static final int DEFAULT_NUM_CONNECTION_INSTANCES = 2;

    private final ConcurrentHashMap<Connection, Gson> configuredInstances =
            new ConcurrentHashMap<>(DEFAULT_NUM_CONNECTION_INSTANCES);
    private final ConcurrentHashMap<Connection, Gson> configuredPrettyPrintedInstances =
            new ConcurrentHashMap<>(DEFAULT_NUM_CONNECTION_INSTANCES);

    /**
     * Creates the configured {@link Gson} instance.
     *
     * @param connection the connection
     * @return the Gson instance
     */
    public Gson getInstance(@NonNull final T connection) {
        Gson instance = configuredInstances.get(connection);
        if (Objects.nonNull(instance)) {
            return instance;
        }

        instance = configure(newGsonBuilder(), connection).create();
        configuredInstances.put(connection, instance);
        return instance;
    }

    /**
     * Gets the configured {@link Gson} instance that provides pretty-printed formatted JSON
     * (i.e., useful for testing and/or debugging).
     *
     * @param connection the connection
     * @return the pre-configured Gson instance
     */
    public Gson getInstanceForPrettyPrinting(@NonNull final T connection) {
        Gson instance = configuredPrettyPrintedInstances.get(connection);
        if (Objects.nonNull(instance)) {
            return instance;
        }

        instance = configure(newGsonBuilder().setPrettyPrinting(), connection).create();
        configuredPrettyPrintedInstances.put(connection, instance);
        return instance;
    }

    /** Clears all cached instances. */
    public void clearAll() {
        configuredInstances.clear();
        configuredPrettyPrintedInstances.clear();
    }

    /**
     * Clears cached instances for the given connection.
     *
     * @param connection the connection
     */
    public void clear(@NonNull final T connection) {
        configuredInstances.remove(connection);
        configuredPrettyPrintedInstances.remove(connection);
    }

    /**
     * Configures a {@link GsonBuilder} for the client type.
     *
     * @param gsonBuilder the GSON builder instance
     * @param connection the connection
     */
    protected abstract GsonBuilder configure(GsonBuilder gsonBuilder, T connection);

    /**
     * Create a new {@link GsonBuilder} instance with the default serialization strategies.
     *
     * @return the builder
     * @see AnnotationBasedExclusionStrategy
     * @see AnnotationBasedSerializationExclusionStrategy
     */
    protected GsonBuilder newGsonBuilder() {
        return new GsonBuilder()
                .setExclusionStrategies(new AnnotationBasedExclusionStrategy())
                .addSerializationExclusionStrategy(new AnnotationBasedSerializationExclusionStrategy());
    }
}
