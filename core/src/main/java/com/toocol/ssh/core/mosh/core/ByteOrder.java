package com.toocol.ssh.core.mosh.core;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 18:32
 * @version: 0.0.1
 */
public class ByteOrder {

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

    /**
     * get long from a big-endian bytes arr
     */
    public static long longBe(byte[] bytes) {
        if (bytes.length != 8) {
            return -1;
        }
        long val = 0;
        for (int i = 0; i < 8; i++) {
            int shift = (7 - i) << 3;
            val |= ((long)0xff<<shift) & ((long)bytes[i] << shift);
        }
        return val;
    }

}
