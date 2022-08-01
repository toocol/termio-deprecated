package com.toocol.termio.utilities.log;

import org.junit.jupiter.api.Test;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/6/28 15:01
 */
class TermioLoggerTest {
    @Test
    void testLog() {
        LoggerFactory.init(null);
        Logger logger = LoggerFactory.getLogger(TermioLoggerTest.class);
        logger.info("Testing log {}", 1);
    }
}