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
