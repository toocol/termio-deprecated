package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.StrUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:06
 */
class ActionCRLF : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.CR, CharEvent.LF)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        val cursorPosition = term.cursorPosition
        term.hideCursor()
        term.setCursorPosition(promptLen, cursorPosition[1])
        term.showCursor()
        term.historyCmdHelper.push(term.lineBuilder.toString())
        term.executeCursorOldX.set(promptLen)
        term.printExecution(StrUtil.EMPTY)
        return true
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return true
    }
}