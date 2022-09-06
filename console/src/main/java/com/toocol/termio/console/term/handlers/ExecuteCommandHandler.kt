package com.toocol.termio.console.term.handlers

import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.commands.TermCommand
import com.toocol.termio.core.term.commands.TermCommand.Companion.cmdOf
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.Printer.printErr
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.module.NonBlockingMessageHandler
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
class ExecuteCommandHandler(vertx: Vertx?, context: Context?) : NonBlockingMessageHandler(
    vertx!!, context!!) {
    private val term = Term.instance
    override fun consume(): IAddress {
        return TermAddress.EXECUTE_OUTSIDE
    }

    override fun <T> handleInline(message: Message<T>) {
        val cmd = message.body().toString()
        val resultAndMessage = Tuple2<Boolean, String?>()
        val isBreak = AtomicBoolean()
        val isCommand = cmdOf(cmd)
            .map { termCommand: TermCommand ->
                try {
                    termCommand.processCmd<Any>(eventBus, cmd, resultAndMessage)
                    if ((TermCommand.CMD_NUMBER == termCommand || TermCommand.CMD_MOSH == termCommand)
                        && resultAndMessage._1()
                    ) {
                        isBreak.set(true)
                    }
                } catch (e: Exception) {
                    printErr("Execute command failed, message = " + e.message)
                }
                true
            }.orElse(false)
        val msg = resultAndMessage._2()
        if (StringUtils.isNotEmpty(msg)) {
            term.printDisplay(msg)
        } else {
            term.cleanDisplayBuffer()
        }
        if (!isCommand && StringUtils.isNotEmpty(cmd)) {
            val builder = AnsiStringBuilder()
                .background(Term.theme.displayBackGroundColor.color)
                .front(Term.theme.commandHighlightColor.color)
                .append(cmd)
                .deFront()
                .append(": command not found.")
            term.printDisplay(builder.toString())
        }
        message.reply(isBreak.get())
    }
}