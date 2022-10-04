package com.toocol.termio.core.term.api

import com.toocol.termio.core.term.commands.TermCommand
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.module.SuspendApi
import com.toocol.termio.utilities.utils.Tuple2
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/2 22:34
 * @version: 0.0.1
 */
object ExecuteCommandApi : SuspendApi {
    suspend fun executeCommand(cmd: String) {
        val resultAndMessage = Tuple2<Boolean, String?>()
        val isBreak = AtomicBoolean()
        val isCommand = TermCommand.cmdOf(cmd)
            .map { termCommand: TermCommand ->
                try {
                    termCommand.processCmd<Any>(cmd, resultAndMessage)
                    if ((TermCommand.CMD_NUMBER == termCommand || TermCommand.CMD_MOSH == termCommand)
                        && StringUtils.isEmpty(resultAndMessage._2())
                    ) {
                        isBreak.set(true)
                    }
                } catch (e: Exception) {
                    Printer.printErr("Execute command failed, message = " + e.message)
                }
                true
            }.orElse(false)
        val msg = resultAndMessage._2()
        if (StringUtils.isNotEmpty(msg)) {
            Term.printDisplay(msg)
        } else {
            Term.cleanDisplayBuffer()
        }
        if (!isCommand && StringUtils.isNotEmpty(cmd)) {
            val builder = AnsiStringBuilder()
                .background(Term.theme.displayBackGroundColor.color)
                .front(Term.theme.commandHighlightColor.color)
                .append(cmd)
                .deFront()
                .append(": command not found.")
            Term.printDisplay(builder.toString())
        }
    }
}