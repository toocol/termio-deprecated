package com.toocol.ssh.common.utils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 21:09
 * @version: 0.0.1
 */
public class Tuple<F> {
    private volatile F first;

    public Tuple(F var1) {
        this.first = var1;
    }

    public Tuple() {
    }

    public F _1() {
        return first;
    }

    public F getFirst() {
        return first;
    }

    public Tuple<F> first(F first) {
        this.first = first;
        return this;
    }

}
