package org.theperkinrex.util.listener;

import java.util.Objects;

public class AnyListenerMap<K, V> implements ListenerMap<K, V> {
    private V value;

    public AnyListenerMap(V value) {
        this.value = value;
    }

    public AnyListenerMap() {
        this(null);
    }

    @Override
    public boolean containsKey(K key) {
        return value != null;
    }

    @Override
    public V get(K key) {
        return value;
    }

    @Override
    public void put(K key, V value) throws PortAlreadyInUseException {
        if (containsKey(key)) throw new PortAlreadyInUseException("Port already in use: has listener for all addresses");
        this.value = value;
    }

    @Override
    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public void remove(K key) {
        this.value = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnyListenerMap<?, ?> that = (AnyListenerMap<?, ?>) o;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AnyListenerMap{" +
                "value=" + value +
                '}';
    }
}
