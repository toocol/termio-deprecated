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

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }
}
