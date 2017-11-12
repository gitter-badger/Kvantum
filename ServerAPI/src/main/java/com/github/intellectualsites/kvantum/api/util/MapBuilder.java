/*
 * Kvantum is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.intellectualsites.kvantum.api.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for initializing {@link Map maps}
 *
 * @param <K> Key type
 * @param <V> Value type
 */
@SuppressWarnings("ALL")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final public class MapBuilder<K, V>
{

    private final Map<K, V> internalMap;
    private final Generator<Map<K, V>, Map<K, V>> generator;

    /**
     * Create a new MapBuilder with {@link HashMap} as the implementation
     *
     * @param <K> Key type
     * @param <V> Value type
     * @return {@link HashMap} builder
     */
    public static <K, V> MapBuilder<K, V> newHashMap()
    {
        return create( map -> new HashMap<>( map ), HashMap::new );
    }

    /**
     * Create a new MapBuilder with {@link LinkedHashMap} as the implementation
     *
     * @param <K> Key type
     * @param <V> Value type
     * @return {@link LinkedHashMap} builder
     */
    public static <K, V> MapBuilder<K, V> newLinkedHashMap()
    {
        return create( map -> new LinkedHashMap<>( map ), LinkedHashMap::new );
    }

    /**
     * Create a new MapBuilder with {@link TreeMap} as the implementation
     *
     * @param <K> Key type
     * @param <V> Value type
     * @return {@link TreeMap} builder
     */
    public static <K, V> MapBuilder<K, V> newTreeMap()
    {
        return create( map -> new TreeMap<>( map ), TreeMap::new );
    }

    private static <K, V> MapBuilder<K, V> create(final Generator<Map<K, V>,
            Map<K, V>> generator, final Provider<Map<K, V>> provider)
    {
        return new MapBuilder<>( provider.provide(), generator );
    }

    /**
     * Delegate for {@link Map#put(Object, Object)}
     * @param key Key
     * @param value Value
     * @return {@code this} builder
     */
    public MapBuilder<K, V> put(final K key, final V value)
    {
        this.internalMap.put( key, value );
        return this;
    }

    /**
     * Delegate for {@link Map#putAll(Object, Object)}
     *
     * @param map Other map
     * @return {@code this} builder
     */
    public MapBuilder<K, V> putAll(final Map<K, V> map)
    {
        this.internalMap.putAll( map );
        return this;
    }

    /**
     * Delegate for {@link Map#remove(Object)}
     * @param key Key
     * @return {@code this} builder
     */
    public MapBuilder<K, V> remove(final K key)
    {
        this.internalMap.remove( key );
        return this;
    }

    /**
     * Delegate for {@link Map#containsKey(Object)}
     *
     * @param k Key
     * @return true if the object exists in the map; else false
     */
    public boolean containsKey(final K k)
    {
        return this.internalMap.containsKey( k );
    }

    /**
     * Get a new copy of the map that the builder has created.
     * This can method be reused as it will return a unique instance.
     * @return new copy
     */
    public Map<K, V> get()
    {
        return generator.generate( this.internalMap );
    }
}
