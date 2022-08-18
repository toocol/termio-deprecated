package com.toocol.termio.core.mosh.core.crypto

import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/13 2:20
 * @version:
 */
internal class AeOcbTest {
    @Test
    fun testEndian() {
        var block = AeOcb.Block.zeroBlock()
        block.l = 123123
        block.r = 123123
        for (b in ByteOrder.longBytes(block.l)) {
            print("$b ")
        }
        println()
        block = AeOcb.Block.swapIfLe(block)
        for (b in ByteOrder.longBytes(block.l)) {
            print("$b ")
        }
    }

    @Test
    fun ntzTest() {
        val bpi = 4
        var blockNum = 0
        for (i in 0..4) {
            blockNum += bpi
            println(AeOcb.ntz(blockNum))
        }
    }
}