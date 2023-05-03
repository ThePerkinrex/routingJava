package org.theperkinrex.util.listener;

public interface ListenerMap<K, V> {
    class PortAlreadyInUseException extends Exception {
        public PortAlreadyInUseException(String message) {
            super(message);
        }
    }
    boolean containsKey(K key);
    V get(K key);
    void put(K key, V value) throws PortAlreadyInUseException;

    boolean isEmpty();

    void remove(K key);
}
