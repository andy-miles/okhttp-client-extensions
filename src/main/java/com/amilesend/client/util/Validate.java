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
package com.amilesend.client.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/** Utility methods used to validate method input. */
@UtilityClass
public class Validate {
    /**
     * Throws an exception if the given {@code chars} is blank.
     *
     * @param chars the character sequence to validate
     * @param message the message to include in the exception thrown
     * @throws NullPointerException if the char sequence is null
     * @throws IllegalArgumentException if the char sequence is not blank
     */
    public static void notBlank(final CharSequence chars, final String message) {
        if (Objects.isNull(chars)) {
            throw new NullPointerException(message);
        }

        if (StringUtils.isBlank(chars)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an exception if the given {@code collection} is empty.
     *
     * @param collection the collection to validate
     * @param message the message to include in the exception thrown
     * @throws NullPointerException if the collection is null
     * @throws IllegalArgumentException if the collection is not blank
     */
    public static void notEmpty(final Collection<?> collection, final String message) {
        if (Objects.isNull(collection)) {
            throw new NullPointerException(message);
        }

        if (collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an exception if the given {@code map} is empty.
     *
     * @param map the map to validate
     * @param message the message to include in the exception thrown
     * @throws NullPointerException if the map is null
     * @throws IllegalArgumentException if the map is not blank
     */
    public static void notEmpty(final Map<?, ?> map, final String message) {
        if (Objects.isNull(map)) {
            throw new NullPointerException(message);
        }

        if (map.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an exception if the given expression is false.
     *
     * @param exp the boolean expression
     * @param message the message to include in the exception thrown
     * @throws IllegalArgumentException if the expression is false
     */
    public static void isTrue(final boolean exp, final String message) {
        if (!exp) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws an exception if the given object is {@code null}.
     *
     * @param obj the object
     * @param message the message to include in the exception thrown
     * @throws NullPointerException if the object is null
     */
    public static void notNull(final Object obj, final String message) {
        Objects.requireNonNull(obj, message);
    }
}
