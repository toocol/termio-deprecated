package com.toocol.ssh.core.mosh.core;

import org.apache.commons.lang3.RandomUtils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:18
 * @version: 0.0.1
 */
public class PRNG {

    public static byte uint8() {
        byte[] x = new byte[1];
        fill(x, 1);
        return x[0];
    }

    public static void fill(byte[] dest, int size) {
        if (size == 0) {
            return;
        }

        byte[] random = RandomUtils.nextBytes(size);
        System.arraycopy(random, 0, dest, 0, size);
    }
}
