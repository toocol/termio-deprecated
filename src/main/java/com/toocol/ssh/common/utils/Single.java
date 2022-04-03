package com.toocol.ssh.common.utils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 21:09
 * @version: 0.0.1
 */
public class Single<V> {

    private volatile V value;

    public Single(V var1) {
        this.value = var1;
    }

    public Single() {
    }

    public V getValue() {
        return value;
    }

    public void valueOf(V first) {
        this.value = first;
    }

}
