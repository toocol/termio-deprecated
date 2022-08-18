package com.toocol.termio.utilities.ansi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/15 0:08
 * @version: 0.0.1
 */
object CursorPositionHelper {
    @JvmStatic
    fun cursorMove(msg: String, line: Int, column: Int): String {
        return "\u001b[${line};${column}H${msg}"
    }
}