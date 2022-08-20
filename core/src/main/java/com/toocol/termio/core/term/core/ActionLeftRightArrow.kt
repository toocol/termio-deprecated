package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.HistoryOutputInfoHelper.Companion.instance
import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.CharUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:01
 */
class ActionLeftRightArrow : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.LEFT_ARROW, CharEvent.RIGHT_ARROW)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        if (Term.status == TermStatus.HISTORY_OUTPUT) {
            if (inChar == CharUtil.LEFT_ARROW) {
                historyOutputInfoHelper.pageLeft()
            } else if (inChar == CharUtil.RIGHT_ARROW) {
                historyOutputInfoHelper.pageRight()
            }
        } else {
            val cursorX = term.cursorPosition[0]
            if (inChar == '\udddd') {
                if (cursorX > promptLen) {
                    term.cursorLeft()
                    term.executeCursorOldX.getAndDecrement()
                }
            } else if (cursorX < term.lineBuilder.length + promptLen) {
                term.cursorRight()
                term.executeCursorOldX.getAndIncrement()
            }
        }
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }

    companion object {
        private val historyOutputInfoHelper = instance
    }
}