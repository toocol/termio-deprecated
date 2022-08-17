package com.toocol.termio.utilities.utils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2021/11/23 15:09
 */
@SuppressWarnings("all")
public final class Tuple3<F, S, T> {

    private F first;

    private S second;

    private T third;

    public Tuple3(F var1, S var2, T third) {
        this.first = var1;
        this.second = var2;
        this.third = third;
    }

    public Tuple3() {
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

    public Tuple3<F, S, T> first(F first) {
        this.first = first;
        return this;
    }

    public Tuple3<F, S, T> second(S second) {
        this.second = second;
        return this;
    }

    public Tuple3<F, S, T> third(T third) {
        this.third = third;
        return this;
    }
}
