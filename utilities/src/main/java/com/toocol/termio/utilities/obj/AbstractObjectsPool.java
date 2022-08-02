package com.toocol.termio.utilities.obj;

import com.toocol.termio.utilities.log.Loggable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/1 22:39
 * @version: 0.0.1
 */
@NotThreadSafe
public abstract class AbstractObjectsPool<T> implements Loggable {

    private static final short INCREMENT = 5;

    protected final AtomicBoolean initOnce = new AtomicBoolean();

    protected final Queue<T> freePool = new ArrayDeque<>();
    protected final Queue<T> busyPool = new ArrayDeque<>();

    public AbstractObjectsPool() {
    }

    /**
     * @return the num of objects in pool
     */
    protected abstract int numObjects();

    /**
     * @return the max num of objects in pool
     */
    protected abstract int maxObjects();

    /**
     * @return new object
     */
    protected abstract T newObject();

    /**
     * initialise the objects pool
     */
    public synchronized void init() {
        if (initOnce.get()) {
            warn("{}: Object pool repeat initialization.", this.getClass().getName());
            return;
        }
        for (int i = 0; i < numObjects(); i++) {
            freePool.offer(newObject());
        }
        initOnce.set(true);
    }

    /**
     * @return get an object from pool
     */
    public T getObject() {
        if (!initOnce.get()) {
            error("{}: Object pool not initialize, getObject() failed", this.getClass().getName());
            return null;
        }
        if (freePool.isEmpty()) {
            if (busyPool.size() >= maxObjects()) {
                return null;
            }
            for (int i = 0; i < INCREMENT; i++) {
                if (freePool.size() + busyPool.size() >= maxObjects()) {
                    break;
                }
                freePool.offer(newObject());
            }
        }
        T poll = freePool.poll();
        busyPool.offer(poll);
        return poll;
    }

    /**
     * recycle the object to the pool
     */
    public void recycle() {
        if (!initOnce.get()) {
            error("{}: Object pool not initialize, recycle() failed", this.getClass().getName());
            return;
        }

        if (busyPool.isEmpty()) {
            return;
        }
        freePool.offer(busyPool.poll());
    }

}
