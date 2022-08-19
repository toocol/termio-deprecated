package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/16 23:54
 * @version: 0.0.1
 */
interface EscapeCodeSequenceSupporter<T> {
    fun registerActions(): List<AnsiEscapeAction<T>>

    fun printOut(text: String)
}