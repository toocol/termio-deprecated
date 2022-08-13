package com.toocol.termio.utilities.utils;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 22:11
 * @version:
 */
class StrUtilTest {

    @Test
    void testSpiltByChinese() {
        String text = "大师大事AJSDjdkslfj附近的斯卡拉附件积分多少卡了LKFfjdsaklFjdsalkf附近的撒开了发j";
        Collection<String> strings = StrUtil.splitSequenceByChinese(text);
        int len = 0;
        for (String string : strings) {
            System.out.println(string + ": " + (StrUtil.isChineseSequenceByHead(string) ? "ch" : "en"));
            len += string.length();
        }
        assertEquals(text.length(), len);
    }
}