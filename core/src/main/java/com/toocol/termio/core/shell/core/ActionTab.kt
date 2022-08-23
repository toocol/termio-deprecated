package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.event.core.CharEvent
import com.toocol.termio.utilities.utils.CharUtil
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:45
 */
class ActionTab : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.TAB)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status == Shell.Status.QUICK_SWITCH) {
            return false
        }
        if (shell.bottomLinePrint.contains(shell.prompt.get())) {
            val cursorPosition = shell.term.cursorPosition
            shell.term.setCursorPosition(shell.currentPrint.length + shell.prompt.get().length, cursorPosition[1])
        }
        if (shell.status == Shell.Status.NORMAL) {
            shell.localLastCmd.delete(0, shell.localLastCmd.length).append(shell.cmd)
            shell.remoteCmd.delete(0, shell.remoteCmd.length).append(shell.cmd)
        }
        shell.localLastInput.delete(0, shell.localLastInput.length).append(localLastInputBuffer)
        localLastInputBuffer.delete(0, localLastInputBuffer.length)
        shell.tabFeedbackRec.clear()
        shell.writeAndFlush(shell.cmd.append(CharUtil.TAB).toString().toByteArray(StandardCharsets.UTF_8))
        shell.cmd.delete(0, shell.cmd.length)
        shell.status = Shell.Status.TAB_ACCOMPLISH
        return false
    }
}