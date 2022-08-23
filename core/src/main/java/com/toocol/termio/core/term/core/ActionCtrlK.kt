package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.event.core.CharEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:04
 */
class ActionCtrlK : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.CTRL_K)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        clear()
        term.lineBuilder.delete(0, term.lineBuilder.length)
        term.executeCursorOldX.set(promptLen)
        term.printScene(false)
        term.cleanDisplayBuffer()
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }
}