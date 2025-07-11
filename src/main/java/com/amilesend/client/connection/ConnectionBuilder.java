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
package com.amilesend.client.connection;

import com.amilesend.client.connection.auth.AuthManager;
import com.amilesend.client.parse.GsonFactoryBase;
import lombok.AccessLevel;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.Validate;

/**
 * Builder to construct new default instances of a {@link Connection}.
 *
 * @see Connection
 */
@Getter(AccessLevel.PROTECTED)
public abstract class ConnectionBuilder<B extends ConnectionBuilder, G extends GsonFactoryBase, C extends Connection<G>> {
    /** The max length of the base URL. */
    public static final int MAX_BASE_URL_STR_LENGTH = 256;

    /** The http client instance. */
    private OkHttpClient httpClient;
    /** The factory used to vend configured Gson instances for a connection. */
    private G gsonFactory;
    /** The authorization manager. */
    private AuthManager<?> authManager;
    /** The base URL of the API. */
    private String baseUrl;
    /** The user-agent header to set for requests. */
    private String userAgent;
    /** Flag indicator to expect Gzip encoded responses. */
    private boolean isGzipContentEncodingEnabled;

    public B httpClient(final OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return (B) this;
    }

    public B gsonFactory(final G gsonFactory) {
        this.gsonFactory = gsonFactory;
        return (B) this;
    }

    public B authManager(final AuthManager<?> authManager) {
        this.authManager = authManager;
        return (B) this;
    }

    public B baseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
        return (B) this;
    }

    public B userAgent(final String userAgent) {
        this.userAgent = userAgent;
        return (B) this;
    }

    public B isGzipContentEncodingEnabled(final boolean isGzipContentEncodingEnabled) {
        this.isGzipContentEncodingEnabled = isGzipContentEncodingEnabled;
        return (B) this;
    }

    public abstract C build();

    protected void validateAttributes() {
        Validate.notNull(httpClient, "httpClient must not be null");
        Validate.notNull(gsonFactory, "gsonFactory must not be null");
        Validate.notNull(authManager, "authManager must not be null");
        Validate.notBlank(baseUrl, "baseUrl must not be blank");
        Validate.isTrue(baseUrl.length() < MAX_BASE_URL_STR_LENGTH,
                "baseUrl length must be less than " + MAX_BASE_URL_STR_LENGTH);
        Validate.notBlank(userAgent, "userAgent must not be blank");
    }
}
