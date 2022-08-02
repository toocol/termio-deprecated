package com.toocol.termio.utilities.anis;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/15 0:08
 * @version: 0.0.1
 */
public final class CursorPositionHelper {
    public static String cursorMove(String msg, int line, int column) {
        return "\u001b[" + line + ";" + column + "H" + msg;
    }
}
