package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.event.core.CharEvent
import com.toocol.termio.utilities.utils.StrUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:44
 */
class ActionCRLF : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.CR, CharEvent.LF)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status == Shell.Status.QUICK_SWITCH) {
            shell.quickSwitchHelper.changeSession()
            return true
        }
        if (shell.status == Shell.Status.TAB_ACCOMPLISH) {
            shell.tabAccomplishLastStroke = StrUtil.EMPTY
            shell.localLastCmd.delete(0, shell.localLastCmd.length).append(shell.remoteCmd).append(StrUtil.CRLF)
        }
        shell.localLastInput.delete(0, shell.localLastInput.length).append(localLastInputBuffer)
        shell.lastRemoteCmd.delete(0, shell.lastRemoteCmd.length).append(shell.remoteCmd.toString())
        shell.lastExecuteCmd.delete(0, shell.lastExecuteCmd.length)
            .append(if (StringUtils.isEmpty(shell.remoteCmd)) shell.cmd.toString() else shell.remoteCmd.toString()
                .replace("\b".toRegex(), ""))
        if (StrUtil.EMPTY != shell.lastExecuteCmd.toString() && (shell.status === Shell.Status.NORMAL || shell.status === Shell.Status.TAB_ACCOMPLISH)) {
            shell.historyCmdHelper.push(shell.lastExecuteCmd.toString())
        }
        if (remoteCursorOffset) {
            shell.cmd.delete(0, shell.cmd.length)
        }
        print(StrUtil.CRLF)
        shell.status = Shell.Status.NORMAL
        return true
    }
}