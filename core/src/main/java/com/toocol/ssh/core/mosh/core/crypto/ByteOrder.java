package com.toocol.ssh.core.mosh.core.crypto;

import java.nio.ByteBuffer;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 18:32
 * @version: 0.0.1
 */
public final class ByteOrder {

    public static java.nio.ByteOrder nativeOrder() {
        return java.nio.ByteOrder.nativeOrder();
    }

    /**
     * whether is the little-endian
     */
    public static boolean littleEndian() {
        return nativeOrder() == java.nio.ByteOrder.LITTLE_ENDIAN;
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
        byte[] bytes = longBytes(x);
        byte[] u32_1 = new byte[4];
        byte[] u32_2 = new byte[4];
        System.arraycopy(bytes, 0, u32_1, 0, 4);
        System.arraycopy(bytes, 4, u32_2, 0, 4);

        int swap1 = bswap32(toInt(u32_1));
        int swap2 = bswap32(toInt(u32_2));

        u32_1 = intBytes(swap1);
        u32_2 = intBytes(swap2);

        System.arraycopy(u32_2, 0, bytes, 0, 4);
        System.arraycopy(u32_1, 0, bytes, 4, 4);

        return bytes;
    }

    public static int bswap32(int x) {
        return (((x) & 0xff000000) >> 24) | (((x) & 0x00ff0000) >>  8) |
                (((x) & 0x0000ff00) <<  8) | (((x) & 0x000000ff) << 24);
    }

    public static byte[] longBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(nativeOrder());
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long toLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8).order(nativeOrder());
        buffer.put(0, bytes);
        return buffer.getLong();
    }

    public static byte[] intBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(nativeOrder());
        buffer.putInt(0, x);
        return buffer.array();
    }

    public static int toInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(4).order(nativeOrder());
        buffer.put(0, bytes);
        return buffer.getInt();
    }
}
