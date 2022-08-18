package com.toocol.termio.utilities.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/13 22:11
 * @version:
 */
internal class StrUtilTest {
    @Test
    fun testSpiltByChinese() {
        val text = "大师大事AJSDjdkslfj附近的斯卡拉附件积分多少卡了LKFfjdsaklFjdsalkf附近的撒开了发j"
        val strings = StrUtil.splitSequenceByChinese(text)
        var len = 0
        for (string in strings) {
            println(string + ": " + if (StrUtil.isChineseSequenceByHead(string)) "ch" else "en")
            len += string.length
        }
        Assertions.assertEquals(text.length, len)
    }

    @Test
    fun testStringProcessing() {
        val withJava = StringWithJava()
        val withKotlin = StringWithKotlin()
        withJava.processingString()
        withKotlin.processString()
    }
}