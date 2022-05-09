package com.toocol.ssh.core.mosh.core.crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 18:32
 * @version: 0.0.1
 */
public class ByteOrder {

    /**
     * whether is the little-endian
     */
    public static boolean littleEndian() {
        return java.nio.ByteOrder.nativeOrder() == java.nio.ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * transfer little-endian long to big-endian byte[]
     */
    public static byte[] htoBe64(long x) {
        return new byte[] {
                (byte) ((x >> 56) & 0xFF),
                (byte) ((x >> 48) & 0xFF),
                (byte) ((x >> 40) & 0xFF),
                (byte) ((x >> 32) & 0xFF),
                (byte) ((x >> 24) & 0xFF),
                (byte) ((x >> 16) & 0xFF),
                (byte) ((x >> 8) & 0xFF),
                (byte) ((x) & 0xFF)
        };
    }

    /**
     * transfer little-endian short to big-endian byte[]
     */
    public static byte[] htoBe16(short x) {
        return new byte[] {
                (byte) ((x >> 8) & 0xFF),
                (byte) ((x) & 0xFF),
        };
    }

    public static byte[] bswap64(long x) {
        byte[] bytes = Longs.toByteArray(x);
        Bytes.reverse(bytes);
        return bytes;
    }
}
