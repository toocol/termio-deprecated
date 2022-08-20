package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.event.CharEvent

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/29 22:46
 * @version: 0.0.1
 */
class ActionEscape : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.ESCAPE)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status != Shell.Status.QUICK_SWITCH) {
            return false
        }
        shell.quickSwitchHelper.quit()
        return true
    }
}