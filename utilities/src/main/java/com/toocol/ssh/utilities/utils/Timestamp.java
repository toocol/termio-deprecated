package com.toocol.ssh.utilities.utils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/2 20:23
 * @version: 0.0.1
 */
public final class Timestamp {
    public static long timestamp() {
        return System.currentTimeMillis();
    }

    public static short timestamp16() {
        short ts = (short) (timestamp() % 65536);
        if (ts == -1) {
            ts++;
        }
        return ts;
    }
}
