package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.event.CharEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:02
 */
class ActionTab : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.TAB)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        val cursorPosition = term.cursorPosition
        if (cursorPosition[0] < term.lineBuilder.length + promptLen) {
            term.setCursorPosition(term.lineBuilder.length + promptLen, cursorPosition[1])
        }
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }
}