package org.theperkinrex.util.listener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ManyListenerMap<K, V> implements ListenerMap<K, V> {
    private final ConcurrentMap<K, V> map;

    public ManyListenerMap() {
        map = new ConcurrentHashMap<>();
    }


    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public void put(K key, V value) throws PortAlreadyInUseException {
        if (containsKey(key)) throw new PortAlreadyInUseException("Port already in use for addr " + key);
        map.put(key, value);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void remove(K key) {
        map.remove(key);
    }

    @Override
    public String toString() {
        return "ManyListenerMap{" +
                "map=" + map +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManyListenerMap<?, ?> that = (ManyListenerMap<?, ?>) o;

        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
