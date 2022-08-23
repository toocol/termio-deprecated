package com.toocol.termio.desktop.components.terminal;

import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 15:10
 * @version:
 */
class TerminalConsoleTextAreaTest {

    @Test
    void testStringEfficiency() {
        var loop = 1000000;
        var testBuilder = new StringBuilder();
        var startTime = System.currentTimeMillis();
        var res = "";
        var input = "Hello World";
        for (int i = 0; i < loop; i++) {
            testBuilder.delete(0, testBuilder.length());
            res = testBuilder.append(StringUtils.join(input, CharUtil.INVISIBLE_CHAR)).append(CharUtil.INVISIBLE_CHAR).toString();
        }
        var endTime = System.currentTimeMillis();
        System.out.println("StringBuilder consume: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            res = StrUtil.join(input, CharUtil.INVISIBLE_CHAR) + CharUtil.INVISIBLE_CHAR;
        }
        endTime = System.currentTimeMillis();
        System.out.println("String splicing consume: " + (endTime - startTime));
    }

}