package com.toocol.ssh.utilities.utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * {@link Enumeration} transfer to {@link Iterator}
 *
 * @author Joezeo
 */
public class EnumerationIter<E> implements Iterator<E>, Iterable<E>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Enumeration<E> e;

    public EnumerationIter(Enumeration<E> enumeration) {
        this.e = enumeration;
    }

    @Override
    public boolean hasNext() {
        return e.hasMoreElements();
    }

    @Override
    public E next() {
        return e.nextElement();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return this;
    }

}
