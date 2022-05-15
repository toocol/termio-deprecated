package com.toocol.ssh.core.mosh.core.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/13 1:19
 * @version:
 */
class ByteOrderTest {

    @Test
    void bswap64Test() {
        long x = 123456;
        x = ByteOrder.toLong(ByteOrder.bswap64(x));
        assertEquals(x, 4675300462675623936L);
    }

    @Test
    void intBytesTest() {
        int x = 0x01000000;
        int y = ByteOrder.toInt(ByteOrder.intBytes(x));
        assertEquals(x, y);
    }
}