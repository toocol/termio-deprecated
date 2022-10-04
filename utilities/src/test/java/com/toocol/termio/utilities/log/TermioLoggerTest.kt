package com.toocol.termio.utilities.log

import org.junit.jupiter.api.Test

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 15:01
 */
internal class TermioLoggerTest {
    @Test
    fun testLog() {
        LoggerFactory.init()
        val logger = LoggerFactory.getLogger()
        logger.info(javaClass.simpleName, "Testing log {}", 1)
    }
}