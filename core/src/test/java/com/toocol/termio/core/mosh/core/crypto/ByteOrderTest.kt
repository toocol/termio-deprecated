package com.toocol.termio.core.mosh.core.crypto

import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/13 1:19
 * @version:
 */
internal class ByteOrderTest {
    @Test
    fun bswap64Test() {
        var x: Long = 123456
        x = ByteOrder.toLong(ByteOrder.bswap64(x))
        Assertions.assertEquals(x, 4675300462675623936L)
    }

    @Test
    fun intBytesTest() {
        val x = 0x01000000
        val y = ByteOrder.toInt(ByteOrder.intBytes(x))
        Assertions.assertEquals(x, y)
    }

    @Test
    fun byteOrderTest() {
        val x: Long = 213123
        Assertions.assertEquals(x, ByteOrder.be64toh(ByteOrder.htoBe64(x)))
        Assertions.assertArrayEquals(ByteOrder.htoBe64(x), Longs.toByteArray(x))
        val y: Short = 123
        Assertions.assertEquals(y, ByteOrder.be16toh(ByteOrder.htoBe16(y)))
        Assertions.assertArrayEquals(ByteOrder.htoBe16(y), Shorts.toByteArray(y))
    }
}