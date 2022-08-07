package com.toocol.termio.utilities.anis;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 22:13
 * @version: 0.0.1
 */
public enum EraseFunctionsMode {
    ERASE_IN_DISPLAY("J", "erase in display (same as ESC[0J)."),
    ERASE_CURSOR_LINE_TO_END("0J", "erase from cursor until end of screen."),
    ERASE_CURSOR_LINE_TO_BEGINNING("1J", "erase from cursor to beginning of screen."),
    ERASE_SCREEN("2J", "erase entire screen."),
    ERASE_SAVED_LINE("3J", "erase saved lines."),
    ERASE_IN_LINE("K", "erase in line (same as ESC[0K)."),
    ERASE_CURSOR_TO_LINE_END("0K", "erase from cursor to end of line."),
    ERASE_CURSOR_TO_LINE_START("1K", "erase start of line to the cursor."),
    ERASE_LINE("2K", "erase the entire line."),
    ;
    public final String code;
    public final String desc;

    EraseFunctionsMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public EraseFunctionsMode codeOf(String code) {
        for (EraseFunctionsMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
