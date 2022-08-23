package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.event.core.CharEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:04
 */
class ActionCtrlU : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.CTRL_U)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        term.lineBuilder.delete(0, term.lineBuilder.length)
        term.executeCursorOldX.set(promptLen)
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }
}