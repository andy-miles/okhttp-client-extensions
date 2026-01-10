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

/**
 * Defines the auth manager for non-authorized/signed requests.
 *
 * @see AuthManager
 * @see NoAuthInfo
 */
public class NoOpAuthManager implements AuthManager<NoAuthInfo> {
    private static final NoAuthInfo NO_AUTH_INFO = new NoAuthInfo();

    @Override
    public NoAuthInfo getAuthInfo() {
        return NO_AUTH_INFO;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public Request.Builder addAuthentication(final Request.Builder requestBuilder) {
        return requestBuilder;
    }
}
