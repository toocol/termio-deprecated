package com.toocol.termio.core.shell.commands

import com.toocol.termio.core.shell.commands.processors.*
import com.toocol.termio.core.shell.core.Shell
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.command.ICommand
import com.toocol.termio.utilities.execeptions.RemoteDisconnectException
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
enum class ShellCommand(private val cmd: String, commandProcessor: ShellCommandProcessor?, comment: String?) : ICommand {
    /**
     * shell's command enums
     */
    DEFAULT(StrUtil.EMPTY, null, null),
    CMD_EXIT("exit", ShellExitCmdProcessor(), "Exit current shell, close ssh connection and destroy connect channel."),
    CMD_HANG("hang", ShellHangCmdProcessor(), "Will not close the connection, exit shell with connection running in the background."),
    CMD_UF("uf", ShellUfCmdProcessor(), "Batch upload local files to remote connection."),
    CMD_DF("df", ShellDfCmdProcessor(), "Batch download remote files to local."),
    CMD_LC("lc", ShellLcCmdProcessor(), "List all the connection properties to quick switch."),
    CMD_CLEAR("clear", ShellClearCmdProcessor(), null);

    private val commandProcessor: ShellCommandProcessor?
    private val comment: String?

    init {
        this.commandProcessor = commandProcessor
        this.comment = comment
    }

    fun processCmd(eventBus: EventBus, shell: Shell, isBreak: AtomicBoolean, msg: String): Tuple2<String?, Long?> {
        if (commandProcessor == null) {
            return Tuple2(StrUtil.EMPTY, null)
        }
        var result = Tuple2<String?, Long?>()
        try {
            if (shell.getRemoteCmd().isNotEmpty()) {
                for (idx in 0 until shell.getRemoteCmd().length) {
                    shell.write('\u007F')
                }
                shell.flush()
            }
            result = commandProcessor.processInner(eventBus, shell, isBreak, msg, this)
        } catch (e: RemoteDisconnectException) {
            isBreak.set(true)
            result.second(shell.sessionId)
        }
        return result
    }

    fun cmd(): String {
        return cmd
    }

    companion object {
        val commands: MutableMap<String, ShellCommand> = HashMap()

        init {
            Arrays.stream(values())
                .forEach { command: ShellCommand ->
                    commands[command.cmd] = command
                }
        }

        @JvmStatic
        fun cmdOf(cmd: String): Optional<ShellCommand> {
            val originCmd = cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), " ").split(" ").toTypedArray()[0]
            val shellCommand = commands[originCmd]
            return Optional.ofNullable(shellCommand)
        }

        @JvmStatic
        fun help(): String {
            val helpBuilder = AnsiStringBuilder().background(Term.theme.displayBackGroundColor.color)
            helpBuilder.append("Shell commands:\t[param] means optional param\n")
            for (command in values()) {
                if (StringUtils.isEmpty(command.comment)) {
                    continue
                }
                helpBuilder.front(Term.theme.commandHighlightColor.color).append(command.cmd).deFront()
                    .append(" ".repeat(23 - command.cmd.length)).append(command.comment!!).append(CharUtil.LF)
            }
            return helpBuilder.toString()
        }
    }
}