package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.event.CharEvent
import com.toocol.termio.utilities.utils.CharUtil
import org.apache.commons.lang3.StringUtils
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:40
 */
class ActionUpDownArrow : ShellCharAction() {
    override fun watch(): Array<CharEvent?> {
        return arrayOf(CharEvent.UP_ARROW, CharEvent.DOWN_ARROW)
    }

    override fun actOn(shell: Shell, charEvent: CharEvent, inChar: Char): Boolean {
        if (shell.status == Shell.Status.QUICK_SWITCH) {
            if (inChar == CharUtil.UP_ARROW) {
                shell.quickSwitchHelper.upSession()
            } else {
                shell.quickSwitchHelper.downSession()
            }
            return true
        }
        shell.status = Shell.Status.NORMAL
        if (inChar == CharUtil.UP_ARROW) {
            if (!shell.historyCmdHelper.isStart) {
                if (shell.cmd.isNotEmpty() && StringUtils.isEmpty(shell.remoteCmd)) {
                    shell.historyCmdHelper.pushToDown(shell.cmd.toString())
                } else if (StringUtils.isNotEmpty(shell.remoteCmd)) {
                    val write = "\u007F".repeat(shell.remoteCmd.length).toByteArray(StandardCharsets.UTF_8)
                    if (write.isNotEmpty()) {
                        shell.writeAndFlush(write)
                        val cmdToPush = shell.remoteCmd.toString().replace("\u007F".toRegex(), "")
                        shell.historyCmdHelper.pushToDown(cmdToPush)
                    }
                }
            }
            shell.historyCmdHelper.up()
        } else {
            shell.historyCmdHelper.down()
        }
        localLastInputBuffer.delete(0, localLastInputBuffer.length).append(shell.cmd)
        shell.localLastCmd.delete(0, shell.localLastCmd.length)
        return false
    }
}