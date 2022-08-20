package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.ansi.Printer.bel
import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.CharUtil
import kotlin.math.max

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 18:03
 */
class ActionBackspace : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.BACKSPACE)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        val cursorPosition = term.cursorPosition
        if (cursorPosition[0] == promptLen) {
            bel()
            return false
        }
        val deleteChar: Char
        if (cursorPosition[0] < term.lineBuilder.length + promptLen) {
            val index = cursorPosition[0] - promptLen - 1
            deleteChar = term.lineBuilder[index]
            term.lineBuilder.deleteCharAt(index)
        } else {
            deleteChar = term.lineBuilder[term.lineBuilder.length - 1]
            term.lineBuilder.deleteCharAt(term.lineBuilder.length - 1)
        }
        term.executeCursorOldX.getAndUpdate { prev: Int ->
            if (CharUtil.isChinese(deleteChar.code)) {
                val `val` = prev - 2
                return@getAndUpdate max(`val`, promptLen)
            } else {
                return@getAndUpdate prev - 1
            }
        }
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        return false
    }
}