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
package com.amilesend.client.connection.auth;

import okhttp3.Request;

import java.util.Objects;

/**
 *  The interface that defines the manager that is responsible for obtaining correct authorization to interact
 *  with the Discogs API.
 */
public interface AuthManager<T extends AuthInfo> {
    /**
     * Retrieves the current authentication info.
     *
     * @return the authentication info
     * @see AuthInfo
     */
    T getAuthInfo();

    /**
     * Gets the authentication state.
     *
     * @return {@code true} if authenticated; else, {@code false}
     */
    default boolean isAuthenticated() {
        return Objects.nonNull(getAuthInfo());
    }

    /**
     * Adds the authentication information to the request being built.
     *
     * @param requestBuilder the request builder
     * @return the builder with the authentication information added
     */
    Request.Builder addAuthentication(Request.Builder requestBuilder);
}
