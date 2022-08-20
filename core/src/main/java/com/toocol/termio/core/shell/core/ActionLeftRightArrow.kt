package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.CharUtil

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:42
 */
class ActionLeftRightArrow : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.LEFT_ARROW, CharEvent.RIGHT_ARROW)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status == Shell.Status.QUICK_SWITCH) {
            return false
        }
        val cursorX = shell.term.cursorPosition[0]
        if (inChar == CharUtil.LEFT_ARROW) {
            if (cursorX > shell.prompt.get().length) {
                shell.term.cursorLeft()
            }
        } else {
            if (cursorX < shell.currentPrint.length + shell.prompt.get().length) {
                shell.term.cursorRight()
            }
        }
        return false
    }
}