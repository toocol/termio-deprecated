package com.toocol.ssh.core.cache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/1 0:08
 * @version: 0.0.1
 */
public class Cache {

    public volatile static boolean HANGED_QUIT = false;

    public volatile static boolean HANGED_ENTER = false;

    public volatile static AtomicInteger HANG_UP_COUNT = new AtomicInteger();

    public static void hangUp() {
        HANG_UP_COUNT.getAndIncrement();
    }

    public static void disconnect() {
        if (HANG_UP_COUNT.get() == 0) {
            return;
        }
        HANG_UP_COUNT.getAndDecrement();
    }

    public static int getHangUp() {
        return HANG_UP_COUNT.get();
    }
}
