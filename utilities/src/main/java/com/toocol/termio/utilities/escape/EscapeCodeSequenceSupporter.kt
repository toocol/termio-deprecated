package com.toocol.termio.utilities.escape

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/16 23:54
 * @version: 0.0.1
 */
interface EscapeCodeSequenceSupporter<T> {
    fun getActionMap(): Map<Class<out IEscapeMode>, AnsiEscapeAction<T>>?

    fun printOut(text: String)
}