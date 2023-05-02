package org.theperkinrex.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentList<P> implements Iterable<P> {
    private final ReadWriteLock lock;
    private final List<P> processes;

    public ConcurrentList() {
        lock = new ReentrantReadWriteLock();
        processes = new LinkedList<>();
    }

    public int add(P p) {
        try {
            lock.writeLock().lock();
            processes.add(p);
            return processes.size()-1;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public P get(int i) throws IndexOutOfBoundsException {
        try {
            lock.readLock().lock();
            return processes.get(i);
        }finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        try {
            lock.readLock().lock();
            return processes.size();
        }finally {
            lock.readLock().unlock();
        }
    }

    private static class ConcurrentIterator<E> implements Iterator<E> {
        private boolean finished;
        private final Lock readLock;
        private final Iterator<E> iterator;

        public ConcurrentIterator(Lock readLock, Iterator<E> iterator) {
            this.finished = false;
            this.readLock = readLock;
            this.readLock.lock();
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return !finished && iterator.hasNext();
        }

        @Override
        public E next() {
            if (finished) throw new NoSuchElementException();
            try {
                return iterator.next();
            } catch (NoSuchElementException e){
                finished = true;
                readLock.unlock();
                throw new NoSuchElementException();
            }
        }
    }

    @NotNull
    @Override
    public Iterator<P> iterator() {
        return new ConcurrentIterator<>(lock.readLock(), processes.iterator());
    }
}
