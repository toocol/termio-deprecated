package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.CharUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 17:59
 */
class ActionUpDownArrow : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.UP_ARROW, CharEvent.DOWN_ARROW)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        if (inChar == CharUtil.UP_ARROW) {
            if (!term.historyCmdHelper.isStart) {
                if (term.lineBuilder.toString().isNotEmpty()) {
                    term.historyCmdHelper.pushToDown(term.lineBuilder.toString())
                }
            }
            val up = term.historyCmdHelper.up()
            if (up != null) {
                term.lineBuilder.delete(0, term.lineBuilder.length).append(up)
            }
        } else {
            val down = term.historyCmdHelper.down()
            if (down != null) {
                term.lineBuilder.delete(0, term.lineBuilder.length).append(down)
            }
        }
        term.executeCursorOldX.set(term.lineBuilder.length + promptLen)
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }
}