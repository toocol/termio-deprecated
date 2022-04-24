package com.toocol.ssh.common.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2021/11/23 15:09
 */
@SuppressWarnings("all")
public final class Tuple2<F, S> {

    private F first;

    private S second;

    public Tuple2(F var1, S var2) {
        this.first = var1;
        this.second = var2;
    }

    public Tuple2() {
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

    public Tuple2<F, S> first(F first) {
        this.first = first;
        return this;
    }

    public Tuple2<F, S> second(S second) {
        this.second = second;
        return this;
    }
}
