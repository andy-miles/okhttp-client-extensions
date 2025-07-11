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
package com.amilesend.client.parse.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * A custom {@link ExclusionStrategy} that is used to configure a {@link com.google.gson.Gson} instance
 * that excludes serialization only for any fields annotated with {@code @GsonSerializeExclude}.
 * @see GsonSerializeExclude
 */
public class AnnotationBasedSerializationExclusionStrategy implements ExclusionStrategy {
    @Override
    public boolean shouldSkipField(final FieldAttributes fieldAttributes) {
        return fieldAttributes.getAnnotation(GsonSerializeExclude.class) != null;
    }

    @Override
    public boolean shouldSkipClass(final Class<?> clazz) {
        return false;
    }
}
