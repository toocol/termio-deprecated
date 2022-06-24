package com.toocol.ssh.utilities.utils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/20 23:39
 * @version: 0.0.1
 */
public interface Asable {

    @SuppressWarnings("all")
    default <T> T as() {
        return (T)this;
    }

}
