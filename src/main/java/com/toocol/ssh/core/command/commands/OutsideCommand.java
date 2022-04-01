package com.toocol.ssh.core.command.commands;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.command.commands.processors.*;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum OutsideCommand {
    /**
     * outside command enums
     */
    CMD_HELP("help", new HelpCmdProcessor(), "Show holistic executive command."),
    CMD_CLEAR("clear", new ClearCmdProcessor(), "Clear the screen."),
    CMD_EXIT("exit", new ExitCmdProcessor(), "Exit ssh terminal."),
    CMD_ADD("add", new AddCmdProcessor(), "Add new ssh connection property. Pattern: 'add --host@user@password[@port]',\n\t\t\tdefault port is 22."),
    CMD_DELETE("delete", new DeleteCmdProcessor(), "Delete ssh connection property. Pattern: 'delete --index', for example: 'delete --1.'"),
    CMD_NUMBER("numbers", new NumberCmdProcessor(), "Select the connection properties.");

    private final String cmd;
    private final AbstractCommandProcessor commandProcessor;
    private final String comment;

    OutsideCommand(String cmd, AbstractCommandProcessor commandProcessor, String comment) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
    }

    public static Optional<OutsideCommand> cmdOf(String cmd) {
        String originCmd = cmd.trim().split(" ")[0].toLowerCase();
        OutsideCommand outsideCommand = null;
        for (OutsideCommand command : values()) {
            if (command.cmd.equals(originCmd)) {
                outsideCommand = command;
            }
        }
        if (StringUtils.isNumeric(originCmd)) {
            outsideCommand = CMD_NUMBER;
        }
        return Optional.ofNullable(outsideCommand);
    }

    @SafeVarargs
    public final <T> void processCmd(EventBus eventBus, T... param) throws Exception {
        if (this.commandProcessor == null) {
            return;
        }
        this.commandProcessor.process(eventBus, param);
    }

    public String cmd() {
        return cmd;
    }

    public static void printHelp() {
        Printer.println();
        Printer.println("Terminal commands:     [param] means optional param");
        for (OutsideCommand command : values()) {
            Printer.println("\t" + command.cmd + "\t\t-- " + command.comment);
        }
        Printer.println();
    }
}
