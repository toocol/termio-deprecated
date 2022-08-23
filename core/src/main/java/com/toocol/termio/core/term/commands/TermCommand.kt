package com.toocol.termio.core.term.commands

import com.toocol.termio.core.term.commands.processors.*
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermTheme.Companion.listTheme
import com.toocol.termio.utilities.ansi.AnsiStringBuilder
import com.toocol.termio.utilities.command.ICommand
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
enum class TermCommand(
    private val cmd: String,
    commandProcessor: TermCommandProcessor?,
    comment: String?,
    specify: String?,
) : ICommand {
    /**
     * outside command enums
     */
    DEFAULT(StrUtil.EMPTY, null, null, null),
    CMD_HELP("help", HelpCmdProcessor(), "Show holistic executive command.", "Show holistic executive command of Termio."),
    CMD_CLEAR("flush", FlushCmdProcessor(), "Flush the screen.", "Refresh and clear the screen"),
    CMD_EXIT("exit", ExitCmdProcessor(), "Exit Termio.", "Exit termio."),
    CMD_THEME("theme", ThemeCmdProcessor(), "Change the Termio's color theme.", """Change the color theme, alternative themes:${listTheme()}""".trimIndent()),
    CMD_ADD("add", AddCmdProcessor(), "Add new ssh connection property.", "add user@host -c=password [-p=port] [-j]\n\n[-p=port] is a optional parameter, the default port is 22.\n[-j] If the remote connection is a JumpServer, you should add this extra parameter."),
    CMD_DELETE("delete", DeleteCmdProcessor(), "Delete ssh connection property.", "Delete a certain connection property.\n\nExamples:\ndelete 1"),
    CMD_STOP("stop", DeleteCmdProcessor(), "Stop ssh connection session.", "Stop connection session.\n\nExamples:\nstop 1"),
    CMD_NUMBER("numbers", NumberCmdProcessor(), "Select the connection properties.", "Input property index to connect"),
    CMD_ACTIVE("active", ActiveCmdProcessor(), "Active the ssh connect session without enter the Shell.", "Active the selected ssh connect session, without enter the Shell.\n\nExamples:\nactive 1\nactive 1 3 4\nactive 1-10"),
    CMD_MOSH("mosh", MoshCmdProcessor(), "Use mosh to connect remote device.", "Use mosh to connect remote device.\n\nExamples:\nmosh 1"),
    CMD_TEST("test", TestCmdProcessor(), "Test console print.", "Test console print."),
    CMD_HISTORY_OUTPUT("history", HistoryCmdProcessor(), "View historical output messages", "View historical output messages"),
    CMD_HELLO_WORLD("hello", HelloCmdProcessor(), null, null);

    private val commandProcessor: TermCommandProcessor?
    private val comment: String?
    val specify: String?

    init {
        this.commandProcessor = commandProcessor
        this.comment = comment
        this.specify = specify
    }

    fun <T> processCmd(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>) {
        if (commandProcessor == null) {
            return
        }
        commandProcessor.processInner(eventBus, cmd, resultAndMsg, this)
    }

    fun cmd(): String {
        return cmd
    }

    companion object {
        @JvmField
        val commands: MutableMap<String, TermCommand> = HashMap()

        init {
            Arrays.stream(values())
                .forEach { command: TermCommand ->
                    if (command == DEFAULT) return@forEach
                    commands[command.cmd] = command
                }
        }

        @JvmStatic
        fun cmdOf(cmd: String): Optional<TermCommand> {
            val originCmd = cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), " ").split(" ").toTypedArray()[0]
            var termCommand = commands[originCmd]
            if (StringUtils.isNumeric(originCmd)) {
                termCommand = CMD_NUMBER
            }
            return Optional.ofNullable(termCommand)
        }

        @JvmStatic
        fun help(): String {
            val helpBuilder = AnsiStringBuilder().background(Term.theme.displayBackGroundColor.color)
            helpBuilder.append("Termio commands:\t[param] means optional param\n")
            for (command in values()) {
                if (StringUtils.isEmpty(command.comment)) {
                    continue
                }
                helpBuilder.front(Term.theme.commandHighlightColor.color).append(command.cmd).deFront()
                    .append(" ".repeat(23 - command.cmd.length)).append(command.comment!!).append(CharUtil.LF)
            }
            helpBuilder.append("\n")
            return helpBuilder.toString()
        }

        @JvmStatic
        fun findAlike(cmd: String): String? {
            for (command in values()) {
                if (command.comment == null) {
                    continue
                }
                if (command.cmd.contains(cmd) || cmd.contains(command.cmd)) {
                    return command.cmd
                }
            }
            return null
        }
    }
}