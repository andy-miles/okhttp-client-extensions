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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

public class NoOpAuthManagerTest {
    private NoOpAuthManager authManagerUnderTest = new NoOpAuthManager();

    @Test
    public void getAuthInfo_shouldReturnNoAuthInfoType() {
        assertEquals(new NoAuthInfo(), authManagerUnderTest.getAuthInfo());
    }

    @Test
    public void addAuthentication_shouldDoNothingAndReturnBuilder() {
        final Request.Builder requestBuilder = mock(Request.Builder.class);

        final Request.Builder actual = authManagerUnderTest.addAuthentication(requestBuilder);

        assertAll(
                () -> assertEquals(requestBuilder, actual),
                () -> verifyNoInteractions(requestBuilder));
    }
}
