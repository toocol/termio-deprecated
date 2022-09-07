package com.toocol.termio.desktop.api.term.handlers

import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.commands.TermCommand
import com.toocol.termio.core.term.commands.TermCommand.Companion.findAlike
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.ansi.ColorHelper.front
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.module.NonBlockingMessageHandler
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import org.apache.commons.lang3.StringUtils

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 2:59
 * @version: 0.0.1
 */
class DynamicEchoHandler(vertx: Vertx?, context: Context?) : NonBlockingMessageHandler(
    vertx!!, context!!) {
    private val credentialCache = CredentialCache.Instance
    private val sshSessionCache = SshSessionCache.Instance
    private val term = Term.instance
    override fun consume(): IAddress {
        return TermAddress.TERMINAL_ECHO
    }

    override fun <T> handleInline(message: Message<T>) {
        val cmd = cast<String>(message.body())
        val commandHighlightColor = Term.theme.commandHighlightColor.color
        val finalCmd = cmd.trim { it <= ' ' }
        val command = COMMANDS[finalCmd]
        if (command == null) {
            if (StringUtils.isEmpty(finalCmd)) {
                lastInput = StrUtil.EMPTY
                term.printDisplayEcho(StrUtil.EMPTY)
                return
            }
            if (StringUtils.isNumeric(finalCmd)) {
                val connectionPrompt = AnsiStringBuilder()
                connectionPrompt.append("Connection [").append(if (finalCmd.length == 1) "0$finalCmd" else finalCmd)
                    .append("]")
                connectionPrompt.append("\n\n")
                val index: Int
                var credential: SshCredential? = null
                try {
                    index = finalCmd.toInt()
                    credential = credentialCache.getCredential(index)
                } catch (e: Exception) {
                    // exceed Integer range
                }
                if (credential == null) {
                    connectionPrompt.append("the index corresponded connection not found.")
                } else {
                    val status =
                        if (sshSessionCache.isAlive(credential.host)) front("alive", Term.theme.sessionAliveColor.color) else "offline"
                    connectionPrompt
                        .append("Host:").append(" ".repeat(15 - 5)).front(Term.theme.hostHighlightColor.color)
                        .append(credential.host).deFront().append("\n")
                        .append("User:").append(" ".repeat(15 - 5)).append(credential.user).append("\n")
                        .append("Port:").append(" ".repeat(15 - 5)).append(credential.port).append("\n")
                        .append("Type:").append(" ".repeat(15 - 5)).append("SSH").append("\n")
                        .append("Status:").append(" ".repeat(15 - 7)).append(status).append("\n")
                }
                if (lastInput != connectionPrompt.toString()) {
                    term.printDisplayEcho(connectionPrompt.toString())
                }
                lastInput = connectionPrompt.toString()
                return
            }
            spaceProcess(term, finalCmd)
        } else {
            if (StringUtils.isNotEmpty(command.specify)) {
                if (lastInput != command.specify) {
                    term.printDisplayEcho(command.specify)
                }
                lastInput = command.specify
            } else {
                val builder = AnsiStringBuilder()
                    .append("Command not found: ")
                    .front(commandHighlightColor).append(cmd).deFront()
                    .append("\n\n")
                    .append("Press ")
                    .front(commandHighlightColor)
                    .append("Ctrl+U").deFront()
                    .append(" to clear input. ")
                if (lastInput != builder.toString()) {
                    term.printDisplayEcho(builder.toString())
                }
                lastInput = builder.toString()
            }
        }
    }

    private fun spaceProcess(term: Term, cmd: String) {
        val commandHighlightColor = Term.theme.commandHighlightColor.color
        if (cmd.contains(StrUtil.SPACE)) {
            val split = cmd.split(StrUtil.SPACE).toTypedArray()
            val splitCommand = COMMANDS[split[0]]
            if (splitCommand == null) {
                val printMsg = AnsiStringBuilder()
                    .append("Command not found: ")
                    .front(commandHighlightColor).append(split[0]).deFront().append("")
                val alikeCommand = findAlike(split[0])
                if (alikeCommand != null) {
                    printMsg.append(", do you mean: ").front(commandHighlightColor).append(alikeCommand).deFront()
                } else {
                    printMsg.append("\n\n")
                        .append("Press ")
                        .front(commandHighlightColor)
                        .append("Ctrl+U").deFront()
                        .append(" to clear input. ")
                }
                if (lastInput != printMsg.toString()) {
                    term.printDisplayEcho(printMsg.toString())
                }
                lastInput = printMsg.toString()
            } else {
                if (StringUtils.isNotEmpty(splitCommand.specify)) {
                    if (lastInput != splitCommand.specify) {
                        term.printDisplayEcho(splitCommand.specify)
                    }
                    lastInput = splitCommand.specify
                }
            }
        } else {
            val titleMsg = AnsiStringBuilder().append("Alternative commands: ")
            val printMsg = AnsiStringBuilder()
            for (value in TermCommand.values()) {
                if (value.cmd().startsWith(cmd)) {
                    if (StringUtils.isNotEmpty(value.specify)) {
                        printMsg.front(commandHighlightColor).append(value.cmd()).deFront().append(CharUtil.TAB)
                    }
                }
            }
            if (printMsg.length() != 0) {
                titleMsg.append(printMsg)
                if (lastInput != titleMsg.toString()) {
                    term.printDisplayEcho(titleMsg.toString())
                }
                lastInput = titleMsg.toString()
            } else {
                val builder = AnsiStringBuilder()
                    .append("Command not found: ")
                    .front(commandHighlightColor).append(cmd).deFront()
                    .append("\n\n")
                    .append("Press ")
                    .front(commandHighlightColor)
                    .append("Ctrl+U").deFront()
                    .append(" to clear input. ")
                if (lastInput != builder.toString()) {
                    term.printDisplayEcho(builder.toString())
                }
                lastInput = builder.toString()
            }
        }
    }

    companion object {
        private val COMMANDS: Map<String, TermCommand> = TermCommand.commands

        @Volatile
        var lastInput: String? = StrUtil.EMPTY
    }
}