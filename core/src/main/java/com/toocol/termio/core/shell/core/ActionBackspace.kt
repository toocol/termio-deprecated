package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.event.core.CharEvent
import com.toocol.termio.utilities.utils.ASCIIStrCache
import com.toocol.termio.utilities.utils.CharUtil
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:46
 */
class ActionBackspace : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.BACKSPACE)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status == Shell.Status.QUICK_SWITCH) {
            return false
        }
        val cursorPosition = shell.getCursorPosition()
        if (cursorPosition[0] <= shell.prompt.get().length) {
            Printer.bel()
            shell.status = Shell.Status.NORMAL
            return false
        }
        if (cursorPosition[0] < shell.currentPrint.length + shell.prompt.get().length) {
            // cursor has moved
            val index = cursorPosition[0] - shell.prompt.get().length - 1
            if (shell.status == Shell.Status.TAB_ACCOMPLISH) {
                var removal = "\u007F".repeat(shell.remoteCmd.length)
                shell.remoteCmd.deleteCharAt(index)
                shell.localLastCmd.delete(0, shell.localLastCmd.length).append(shell.remoteCmd)
                removal += shell.remoteCmd.toString()
                shell.tabAccomplishLastStroke = ASCIIStrCache.toString(inChar)
                shell.writeAndFlush(removal.toByteArray(StandardCharsets.UTF_8))
                remoteCursorOffset = true
            }
            if (shell.status == Shell.Status.NORMAL) {
                shell.cmd.deleteCharAt(index)
            }
            shell.currentPrint.deleteCharAt(index)
            shell.hideCursor()
            Printer.virtualBackspace()
            print(shell.currentPrint.substring(index, shell.currentPrint.length) + CharUtil.SPACE)
            shell.setCursorPosition(cursorPosition[0] - 1, cursorPosition[1])
            shell.showCursor()
        } else {
            if (localLastInputBuffer.isNotEmpty()) {
                localLastInputBuffer.deleteCharAt(localLastInputBuffer.length - 1)
            }
            if (shell.status == Shell.Status.TAB_ACCOMPLISH) {
                // This is ctrl+backspace
                shell.writeAndFlush('\u007F')
                if (shell.remoteCmd.isNotEmpty()) {
                    val newVal = shell.remoteCmd.toString().substring(0, shell.remoteCmd.length - 1)
                    shell.remoteCmd.delete(0, shell.remoteCmd.length).append(newVal)
                }
                if (shell.localLastCmd.isNotEmpty()) {
                    val newVal = shell.localLastCmd.toString().substring(0, shell.localLastCmd.length - 1)
                    shell.localLastCmd.delete(0, shell.localLastCmd.length).append(newVal)
                }
            }
            if (shell.status == Shell.Status.NORMAL) {
                shell.cmd.deleteCharAt(shell.cmd.length - 1)
            }
            if (shell.currentPrint.isNotEmpty()) {
                val newVal = shell.currentPrint.toString().substring(0, shell.currentPrint.length - 1)
                shell.currentPrint.delete(0, shell.currentPrint.length).append(newVal)
            }
            Printer.virtualBackspace()
        }
        return false
    }
}