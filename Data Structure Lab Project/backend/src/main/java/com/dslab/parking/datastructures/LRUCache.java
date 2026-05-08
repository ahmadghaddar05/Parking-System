package com.dslab.parking.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <h2>LRUCache&lt;K, V&gt;</h2>
 *
 * <p>A bounded Least-Recently-Used cache built on top of {@link LinkedHashMap}.
 * Time complexity:
 * <ul>
 *   <li>{@link #get(Object)}: O(1) amortized</li>
 *   <li>{@link #put(Object, Object)}: O(1) amortized</li>
 * </ul>
 *
 * <p><b>Why a HashMap alone is not enough:</b> a plain HashMap has no notion of
 * recency. {@code LinkedHashMap} keeps insertion/access order in a doubly linked
 * list while preserving O(1) lookup, which is exactly the textbook LRU
 * structure: hash table for O(1) access + linked list for ordering.
 *
 * <p>Used here to cache parking-lot rows that drivers view often, so the
 * /lots/nearby endpoint does not hit MySQL on every request.
 */
public class LRUCache<K, V> {

    private final int capacity;
    private final LinkedHashMap<K, V> map;

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.capacity = capacity;
        // accessOrder = true -> reorders on get(), giving real LRU semantics
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.capacity;
            }
        };
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized void invalidate(K key) {
        map.remove(key);
    }

    public synchronized void clear() {
        map.clear();
    }

    public synchronized int size() {
        return map.size();
    }

    public int capacity() {
        return capacity;
    }
}
