package com.toocol.ssh.core.mosh.core.crypto;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

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
        byte[] bytes = ByteOrder.longBytes(x);
        x = ByteOrder.toLong(ByteOrder.bswap64(x));
        assertEquals(x, 4675300462675623936L);
    }

}