package com.toocol.ssh.core.mosh.core.crypto;

import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import org.junit.jupiter.api.Test;

import static com.toocol.ssh.core.mosh.core.crypto.ByteOrder.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
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

    @Test
    public void byteOrderTest() {
        long x = 213123;
        assertEquals(x, be64toh(htoBe64(x)));
        assertArrayEquals(htoBe64(x), Longs.toByteArray(x));

        short y = 123;
        assertEquals(y, be16toh(htoBe16(y)));
        assertArrayEquals(htoBe16(y), Shorts.toByteArray(y));
    }

}