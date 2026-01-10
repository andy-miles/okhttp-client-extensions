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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

/**
 * A simple tuple class that references two objects.
 *
 * @param <K> the key or left
 * @param <V> the value or right
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Pair<K, V> implements Map.Entry<K, V> {
    /** The left reference. */
    final K left;
    /** the right reference. */
    final V right;

    /**
     * Creates a new {@code Pair} for the given left and right objects.
     *
     * @param left the left object
     * @param right the right object
     * @return the tuple
     * @param <K> the left object class type
     * @param <V> the right object class type
     */
    public static <K, V> Pair<K, V> of(final K left, final V right) {
        return new Pair<>(left, right);
    }

    @Override
    public K getKey() {
        return left;
    }

    @Override
    public V getValue() {
        return right;
    }

    @Override
    public V setValue(final V value) {
        throw new UnsupportedOperationException("Pair is immutable");
    }
}
