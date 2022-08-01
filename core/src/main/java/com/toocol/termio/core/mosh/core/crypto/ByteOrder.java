package com.toocol.termio.core.mosh.core.crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

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
     * whether is the little-endian.
     */
    public static boolean littleEndian() {
        return nativeOrder() == java.nio.ByteOrder.LITTLE_ENDIAN;
    }

    /**
     * transfer little-endian long to big-endian byte[].
     */
    public static byte[] htoBe64(long x) {
        return new byte[]{
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
     * transfer big-endian bytes to long.
     */
    public static long be64toh(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }

    /**
     * transfer little-endian short to big-endian byte[].
     */
    public static byte[] htoBe16(short x) {
        return new byte[]{
                (byte) ((x >> 8) & 0xFF),
                (byte) ((x) & 0xFF),
        };
    }

    /**
     * transfer big-endian bytes to short.
     */
    public static short be16toh(byte[] bytes) {
        return Shorts.fromByteArray(bytes);
    }

    public static byte[] bswap64(long x) {
        byte[] array = longBytes(x);
        Bytes.reverse(array);
        return array;
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
