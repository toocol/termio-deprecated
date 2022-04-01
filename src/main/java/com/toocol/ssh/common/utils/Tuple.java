package com.toocol.ssh.common.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2021/11/23 15:09
 */
@SuppressWarnings("all")
public class Tuple<F, S> {

    private F first;

    private S second;

    public Tuple(F var1, S var2) {
        this.first = var1;
        this.second = var2;
    }

    public Tuple() {
    }

    public F _1() {
        return first;
    }

    public S _2() {
        return second;
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    public Tuple<F, S> first(F first) {
        this.first = first;
        return this;
    }

    public Tuple<F, S> second(S second) {
        this.second = second;
        return this;
    }
}
