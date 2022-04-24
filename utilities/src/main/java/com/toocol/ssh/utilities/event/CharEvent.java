package com.toocol.ssh.utilities.event;

import com.toocol.ssh.utilities.utils.CharUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 10:11
 */
public enum CharEvent {
    UP_ARROW(CharUtil.UP_ARROW),
    DOWN_ARROW(CharUtil.DOWN_ARROW),
    LEFT_ARROW(CharUtil.LEFT_ARROW),
    RIGHT_ARROW(CharUtil.RIGHT_ARROW),
    TAB(CharUtil.TAB),
    BACKSPACE(CharUtil.BACKSPACE),
    CR(CharUtil.CR),
    LF(CharUtil.LF),
    ASCII_PRINTABLE('\0'),
    CHINESE_CHARACTER('\0')
    ;
    public final char represent;

    public static CharEvent eventOf(char ch) {
        if (CharUtil.isAsciiPrintable(ch)) {
            return ASCII_PRINTABLE;
        }
        if (CharUtil.isChinese(ch)) {
            return CHINESE_CHARACTER;
        }
        for (CharEvent event : values()) {
            if (ch == event.represent) {
                return event;
            }
        }
        return null;
    }

    CharEvent(char represent) {
        this.represent = represent;
    }
}
