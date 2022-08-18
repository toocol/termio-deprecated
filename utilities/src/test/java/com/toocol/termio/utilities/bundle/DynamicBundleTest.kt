package com.toocol.termio.utilities.bundle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/10 22:46
 * @version:
 */
class DynamicBundleTest {

    @Test
    void testDynamicBundle() {
        assertDoesNotThrow(() -> {
            TestDynamicBundle bundle = new TestDynamicBundle();
            assertNull(bundle.message("test.key"));
        });
    }

}