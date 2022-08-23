package com.toocol.termio.core.term.core

import com.toocol.termio.utilities.event.core.CharEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:06
 */
class ActionEscape : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.ESCAPE)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        if (TermStatus.HISTORY_OUTPUT == Term.status) {
            term.cleanDisplay()
            Term.status = TermStatus.TERMIO
        }
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }
}