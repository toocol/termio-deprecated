package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.event.core.CharEvent
import com.toocol.termio.utilities.utils.ASCIIStrCache
import com.toocol.termio.utilities.utils.CharUtil
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:43
 */
class ActionAsciiPrintable : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.ASCII_PRINTABLE)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status == Shell.Status.QUICK_SWITCH) {
            return false
        }
        if (shell.escapeHelper.isAcceptBracketAfterEscape) {
            return false
        }
        if (inChar == CharUtil.SPACE && shell.currentPrint.isEmpty()) {
            return false
        }
        val cursorPosition = shell.getCursorPosition()
        if (cursorPosition[0] < shell.currentPrint.length + shell.prompt.get().length) {
            // cursor has moved
            val index = cursorPosition[0] - shell.prompt.get().length
            if (index == 0 && inChar == CharUtil.SPACE) {
                return false
            }
            if (shell.status == Shell.Status.TAB_ACCOMPLISH) {
                var removal = "\u007F".repeat(shell.remoteCmd.length)
                shell.remoteCmd.insert(index, inChar)
                shell.localLastCmd.delete(0, shell.localLastCmd.length).append(shell.remoteCmd)
                shell.tabAccomplishLastStroke = ASCIIStrCache.toString(inChar)
                removal += shell.remoteCmd.toString()
                shell.writeAndFlush(removal.toByteArray(StandardCharsets.UTF_8))
                remoteCursorOffset = true
            } else {
                shell.cmd.insert(index, inChar)
                localLastInputBuffer.insert(index, inChar)
            }
            shell.currentPrint.insert(index, inChar)
            shell.hideCursor()
            print(shell.currentPrint.substring(index, shell.currentPrint.length))
            shell.setCursorPosition(cursorPosition[0] + 1, cursorPosition[1])
            shell.showCursor()
        } else {
            // cursor hasn't moved
            if (shell.status == Shell.Status.TAB_ACCOMPLISH) {
                shell.remoteCmd.append(inChar)
                shell.localLastCmd.append(inChar)
                shell.tabAccomplishLastStroke = ASCIIStrCache.toString(inChar)
                shell.writeAndFlush(inChar)
            } else {
                shell.cmd.append(inChar)
            }
            shell.currentPrint.append(inChar)
            localLastInputBuffer.append(inChar)
            print(ASCIIStrCache.toString(inChar))
        }
        return false
    }
}