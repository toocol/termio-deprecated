package com.toocol.termio.utilities.bundle

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:46
 * @version:
 */
internal class DynamicBundleTest {
    @Test
    fun testDynamicBundle() {
        Assertions.assertDoesNotThrow {
            val bundle = TestDynamicBundle()
            Assertions.assertNull(bundle.message("test.key"))
        }
    }
}