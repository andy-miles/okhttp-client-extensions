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
package com.amilesend.client.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;

/** Simple string utility methods. */
@UtilityClass
public class StringUtils {
    public static final String EMPTY = "";

    /**
     * Determines if the given char sequence is blank.
     *
     * @param cs the char sequence to evaluate
     * @return {@code true} if blank; else, {@code false}
     */
    public static boolean isBlank(final CharSequence cs) {
        if (Objects.isNull(cs) || cs.length() == 0) {
            return true;
        }

        for (int i = 0; i < cs.length(); ++i) {
            final char c = cs.charAt(i);
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines if the given char sequence is not blank.
     *
     * @param cs the char sequence to evaluate
     * @return {@code true} if not blank; else, {@code false}
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Determines if the given char sequence is empty.
     *
     * @param cs the char sequence to evaluate
     * @return {@code true} if empty; else, {@code false}
     */
    public static boolean isEmpty(final CharSequence cs) {
        return Objects.isNull(cs) || cs.length() == 0;
    }

    /**
     * Determines if the given char sequence is not empty.
     *
     * @param cs the char sequence to evaluate
     * @return {@code true} if not empty; else, {@code false}
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }
}
