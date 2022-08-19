package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/19 19:25
 * @version: 0.0.1
 */
class TestEscapeSupporter: EscapeCodeSequenceSupporter<TestEscapeSupporter> {
    override fun printOut(text: String) {

    }

    override fun getActionMap(): Map<Class<out IEscapeMode>, AnsiEscapeAction<TestEscapeSupporter>> {
        return mutableMapOf()
    }
}