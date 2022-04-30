package com.toocol.ssh.core.mosh.core;

/**
 * byteorder.h
 *
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 18:32
 * @version: 0.0.1
 */
public class ByteOrder {

    /**
     * maybe, it is not sure whether this is right
     */
    public static byte[] htobe64(long x) {
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

}
