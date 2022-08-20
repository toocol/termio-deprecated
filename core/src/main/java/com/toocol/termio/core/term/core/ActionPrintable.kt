package com.toocol.termio.core.term.core

import com.toocol.termio.core.term.core.Term.Companion.promptLen
import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.CharUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 17:55
 */
class ActionPrintable : TermCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.ASCII_PRINTABLE, CharEvent.CHINESE_CHARACTER)
    }

    override fun actOnConsole(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        if (term.escapeHelper.isAcceptBracketAfterEscape) {
            return false
        }
        if (inChar == CharUtil.SPACE && term.lineBuilder.isEmpty()) {
            return false
        }
        val cursorX = term.executeCursorOldX.get()
        if (cursorX >= Term.width - 1) {
            return false
        }
        if (cursorX < term.lineBuilder.length + promptLen) {
            val index = cursorX - promptLen
            if (index == 0 && inChar == CharUtil.SPACE) {
                return false
            }
            term.lineBuilder.insert(index, inChar)
        } else {
            term.lineBuilder.append(inChar)
        }
        term.executeCursorOldX.getAndUpdate { prev: Int ->
            if (CharUtil.isChinese(inChar.code)) {
                return@getAndUpdate prev + 2
            } else {
                return@getAndUpdate prev + 1
            }
        }
        return false
    }

    override fun actOnDesktop(term: Term, charEvent: CharEvent, inChar: Char): Boolean {
        term.lineBuilder.append(inChar)
        return false
    }
}