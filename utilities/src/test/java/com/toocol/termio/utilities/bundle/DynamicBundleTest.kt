package com.toocol.termio.utilities.bundle

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:46
 * @version:
 */
internal class DynamicBundleTest {
    @Test
    fun testDynamicBundle() {
        Assertions.assertDoesNotThrow {
            val (contentParam1, contentParam2, keyIdx) = arrayOf("content1", "content2", 1)
            println(TestDynamicBundle1.message(Locale.CHINESE, "key-$keyIdx", contentParam1, contentParam2))
            println(TestDynamicBundle2.message(Locale.ENGLISH, "key"))
            println(TestDynamicBundle3.message(key = "key-$keyIdx", fillParams = arrayOf(contentParam1)))
            println(TestDynamicBundle4.message(key = "key"))
        }
    }
}