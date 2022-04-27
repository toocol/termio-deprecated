package com.toocol.ssh.core.term.commands;

import com.toocol.ssh.core.term.commands.processors.*;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.command.ICommand;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.Tuple2;
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
public enum TermioCommand implements ICommand {
    /**
     * outside command enums
     */
    CMD_HELP("help", new HelpCmdProcessor(), "Show holistic executive command.", "Show holistic executive command of Termio."),
    CMD_CLEAR("flush", new FlushCmdProcessor(), "Flush the screen.", "Refresh and clear the screen"),
    CMD_EXIT("exit", new ExitCmdProcessor(), "Exit Termio.", "Exit termio."),
    CMD_THEME("theme", new ThemeCmdProcessor(), "Change the Termio's color theme.", "Change the color theme, current support [dark/light]\nExamples:\ntheme dark\ntheme light"),
    CMD_ADD("add", new AddCmdProcessor(), "Add new ssh connection property.", "add --user@host -c=password [-p=port]\n\n-p=port is a optional parameter, the default port is 22."),
    CMD_DELETE("delete", new DeleteCmdProcessor(), "Delete ssh connection property.", "Delete a certain connection property.\n\nExamples:\ndelete --1"),
    CMD_NUMBER("numbers", new NumberCmdProcessor(), "Select the connection properties.", "Input property index to connect"),
    CMD_ACTIVE("active", new ActiveCmdProcessor(), "Active the selected ssh connect session, without enter the Shell.", "Active the selected ssh connect session, without enter the Shell.\n\nExamples:\nactive 1\nactive 1 3 4\nactive 1-10"),
    CMD_HELLO_WORLD("hello", new HelloCmdProcessor(), null, null);

    public static final Map<String, TermioCommand> COMMANDS = new HashMap<>();

    static {
        Arrays.stream(values())
                .forEach(command -> COMMANDS.put(command.cmd, command));
    }

    private final String cmd;
    private final OutsideCommandProcessor commandProcessor;
    private final String comment;
    private final String specify;

    TermioCommand(String cmd, OutsideCommandProcessor commandProcessor, String comment, String specify) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
        this.specify = specify;
    }

    public static Optional<TermioCommand> cmdOf(String cmd) {
        String originCmd = cmd.trim().replaceAll(" {2,}", " ").split(" ")[0];
        TermioCommand termioCommand = COMMANDS.get(originCmd);
        if (StringUtils.isNumeric(originCmd)) {
            termioCommand = CMD_NUMBER;
        }
        return Optional.ofNullable(termioCommand);
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

    public static String help() {
        AnisStringBuilder helpBuilder = new AnisStringBuilder().background(Term.theme.displayBackGroundColor);
        helpBuilder.append("Termio commands:\t[param] means optional param\n");
        for (TermioCommand command : values()) {
            if (StringUtils.isEmpty(command.comment)) {
                continue;
            }
            helpBuilder.front(Term.theme.commandHighlightColor).append(command.cmd).deFront()
                    .append(" ".repeat(23 - command.cmd.length())).append(command.comment).append(CharUtil.LF);
        }
        helpBuilder.append("\n");
        return helpBuilder.toString();
    }

    public static String findAlike(String cmd) {
        for (TermioCommand command : values()) {
            if (command.comment == null) {
                continue;
            }
            if (command.cmd.contains(cmd) || cmd.contains(command.cmd)) {
                return command.cmd;
            }
        }
        return null;
    }

    public String getSpecify() {
        return specify;
    }
}
