package com.toocol.termio.core.mosh.core.crypto;

import com.toocol.termio.utilities.execeptions.CryptoException;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/8 21:18
 * @version: 0.0.1
 */
public final class Prng {
    public static byte uint8() {
        byte[] x = new byte[1];
        fill(x, 1);
        return x[0];
    }

    public static void fill(byte[] dest, int size) {
        if (size == 0) {
            return;
        }

        byte[] random = nextBytes(size);
        System.arraycopy(random, 0, dest, 0, size);
    }

    private static byte[] nextBytes(int count) {
        if (count < 0) {
            throw new CryptoException("Count is negative.");
        }

        byte[] bytes = new byte[count];
        for (int i = 0; i < count; i++) {
            bytes[i] = (byte) RandomUtils.nextInt(0, Byte.MAX_VALUE + 1);
        }
        return bytes;
    }
}
