package com.toocol.termio.core.term.commands;

import com.toocol.termio.core.term.commands.processors.*;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermTheme;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.command.ICommand;
import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum TermCommand implements ICommand {
    /**
     * outside command enums
     */
    CMD_HELP("help", new HelpCmdProcessor(), "Show holistic executive command.", "Show holistic executive command of Termio."),
    CMD_CLEAR("flush", new FlushCmdProcessor(), "Flush the screen.", "Refresh and clear the screen"),
    CMD_EXIT("exit", new ExitCmdProcessor(), "Exit Termio.", "Exit termio."),
    CMD_THEME("theme", new ThemeCmdProcessor(), "Change the Termio's color theme.", "Change the color theme, alternative themes:\n\n" + TermTheme.listTheme()),
    CMD_ADD("add", new AddCmdProcessor(), "Add new ssh connection property.", "add user@host -c=password [-p=port] [-j]\n\n[-p=port] is a optional parameter, the default port is 22.\n[-j] If the remote connection is a JumpServer, you should add this extra parameter."),
    CMD_DELETE("delete", new DeleteCmdProcessor(), "Delete ssh connection property.", "Delete a certain connection property.\n\nExamples:\ndelete 1"),
    CMD_stop("stop", new DeleteCmdProcessor(), "Stop ssh connection session.", "Stop connection session.\n\nExamples:\nstop 1"),
    CMD_NUMBER("numbers", new NumberCmdProcessor(), "Select the connection properties.", "Input property index to connect"),
    CMD_ACTIVE("active", new ActiveCmdProcessor(), "Active the ssh connect session without enter the Shell.", "Active the selected ssh connect session, without enter the Shell.\n\nExamples:\nactive 1\nactive 1 3 4\nactive 1-10"),
    CMD_MOSH("mosh", new MoshCmdProcessor(), "Use mosh to connect remote device.", "Use mosh to connect remote device.\n\nExamples:\nmosh 1"),
    CMD_TEST("test", new TestCmdProcessor(), "Test console print.", "Test console print."),
    CMD_COLOR("color", null, "256 Color panel for testing.", "256 Color panel for testing."),
    CMD_HELLO_WORLD("hello", new HelloCmdProcessor(), null, null),
    CMD_HISTORY_OUTPUT("history",new HistoryCmdProcessor(),"View historical output messages","View historical output messages");
    public static final Map<String, TermCommand> COMMANDS = new HashMap<>();

    static {
        Arrays.stream(values())
                .forEach(command -> COMMANDS.put(command.cmd, command));
    }

    private final String cmd;
    private final TermCommandProcessor commandProcessor;
    private final String comment;
    private final String specify;

    TermCommand(String cmd, TermCommandProcessor commandProcessor, String comment, String specify) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
        this.specify = specify;
    }

    public static Optional<TermCommand> cmdOf(String cmd) {
        String originCmd = cmd.trim().replaceAll(" {2,}", " ").split(" ")[0];
        TermCommand termCommand = COMMANDS.get(originCmd);
        if (StringUtils.isNumeric(originCmd)) {
            termCommand = CMD_NUMBER;
        }
        return Optional.ofNullable(termCommand);
    }

    public static String help() {
        AnsiStringBuilder helpBuilder = new AnsiStringBuilder().background(Term.theme.displayBackGroundColor.color);
        helpBuilder.append("Termio commands:\t[param] means optional param\n");
        for (TermCommand command : values()) {
            if (StringUtils.isEmpty(command.comment)) {
                continue;
            }
            helpBuilder.front(Term.theme.commandHighlightColor.color).append(command.cmd).deFront()
                    .append(" ".repeat(23 - command.cmd.length())).append(command.comment).append(CharUtil.LF);
        }
        helpBuilder.append("\n");
        return helpBuilder.toString();
    }

    public static String findAlike(String cmd) {
        for (TermCommand command : values()) {
            if (command.comment == null) {
                continue;
            }
            if (command.cmd.contains(cmd) || cmd.contains(command.cmd)) {
                return command.cmd;
            }
        }
        return null;
    }

    public final <T> void processCmd(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        if (this.commandProcessor == null) {
            return;
        }
        this.commandProcessor.process(eventBus, cmd, resultAndMsg);
    }

    public String cmd() {
        return cmd;
    }

    public String getSpecify() {
        return specify;
    }
}
