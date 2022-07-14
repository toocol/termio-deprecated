package com.toocol.ssh.utilities.utils;

/**
 * The filter interface
 *
 * @author Joezeo
 */
@FunctionalInterface
public interface Filter<T> {
    /**
     * is accept obj
     *
     * @param t the obj to accept
     * @return is accept obj
     */
    boolean accept(T t);
}