package com.toocol.ssh.core.term.commands;

import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.commands.processors.*;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum TermioCommand {
    /**
     * outside command enums
     */
    CMD_HELP("help", new HelpCmdProcessor(), "Show holistic executive command."),
    CMD_CLEAR("clear", new ClearCmdProcessor(), "Clear the screen."),
    CMD_EXIT("exit", new ExitCmdProcessor(), "Exit Termio."),
    CMD_ADD("add", new AddCmdProcessor(), "Add new ssh connection property. Pattern: 'add --user@host -c=password [-p=port]',default port is 22."),
    CMD_DELETE("delete", new DeleteCmdProcessor(), "Delete ssh connection property. Pattern: 'delete --index', for example: 'delete --1'."),
    CMD_NUMBER("numbers", new NumberCmdProcessor(), "Select the connection properties."),
    ;

    private static final String[] tabBuffer = new String[]{"\t\t\t", "\t\t", "\t"};

    private final String cmd;
    private final OutsideCommandProcessor commandProcessor;
    private final String comment;

    TermioCommand(String cmd, OutsideCommandProcessor commandProcessor, String comment) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
    }

    public static Optional<TermioCommand> cmdOf(String cmd) {
        String originCmd = cmd.trim().replaceAll(" {2,}"," ").split(" ")[0];
        TermioCommand termioCommand = null;
        for (TermioCommand command : values()) {
            if (command.cmd.equals(originCmd)) {
                termioCommand = command;
            }
        }
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

    public static void printHelp() {
        Printer.println("Termio commands:\t[param] means optional param");
        for (TermioCommand command : values()) {
            Printer.printColor(command.cmd, 78);
            Printer.println(tabBuffer[command.cmd.length() / 8] + command.comment);
        }
        Printer.println();
    }
}
