package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.utils.CharUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 10:11
 */
enum class CharEvent(val represent: Char) {
    UP_ARROW(CharUtil.UP_ARROW),
    DOWN_ARROW(CharUtil.DOWN_ARROW),
    LEFT_ARROW(CharUtil.LEFT_ARROW),
    RIGHT_ARROW(CharUtil.RIGHT_ARROW),
    TAB(CharUtil.TAB),
    BACKSPACE(CharUtil.BACKSPACE),
    CR(CharUtil.CR),
    LF(CharUtil.LF),
    CTRL_U(CharUtil.CTRL_U),
    CTRL_K(CharUtil.CTRL_K),
    ESCAPE(CharUtil.ESCAPE),
    ASCII_PRINTABLE('\u0000'),
    CHINESE_CHARACTER('\u0000');

    companion object {
        @JvmStatic
        fun eventOf(ch: Char): CharEvent? {
            if (CharUtil.isAsciiPrintable(ch)) {
                return ASCII_PRINTABLE
            }
            if (CharUtil.isChinese(ch.code)) {
                return CHINESE_CHARACTER
            }
            for (event in values()) {
                if (ch == event.represent) {
                    return event
                }
            }
            return null
        }
    }
}