package com.btk5h.skriptmirror.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementation of reflect LRU cache that
 * always computes new values when {@link #computeIfAbsent(Object, Function)}
 * is called.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private final int cacheSize;

    public LRUCache(int cacheSize) {
        super(16, 0.75F, true);
        this.cacheSize = cacheSize;
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return mappingFunction.apply(key);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= cacheSize;
    }

}
