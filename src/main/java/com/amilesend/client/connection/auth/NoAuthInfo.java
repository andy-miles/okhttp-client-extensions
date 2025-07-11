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
package com.amilesend.client.connection.auth;

/** Defines having no authorization information. This assumes that requests are not authenticated or signed. */
public class NoAuthInfo implements AuthInfo {
    private static final int HASH_VALUE = 1;

    @Override
    public int hashCode() {
        return HASH_VALUE;
    }

    @Override
    public boolean equals(final Object thatObj) {
        return NoAuthInfo.class.isInstance(thatObj);
    }
}
