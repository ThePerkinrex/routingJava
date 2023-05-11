package org.theperkinrex.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleElementIterator<E> implements Iterator<E> {
    private E element;

    public SingleElementIterator(E element) {
        this.element = element;
    }

    @Override
    public boolean hasNext() {
        return element != null;
    }

    @Override
    public E next() {
        if(!hasNext()) throw new NoSuchElementException();
        return element;
    }
}
