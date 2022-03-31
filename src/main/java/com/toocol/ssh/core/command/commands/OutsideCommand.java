package com.toocol.ssh.core.command.commands;

import com.toocol.ssh.core.command.commands.processors.ClearCmdProcessor;
import com.toocol.ssh.core.command.commands.processors.ConcCmdProcessor;
import com.toocol.ssh.core.command.commands.processors.ExitCmdProcessor;
import com.toocol.ssh.core.command.commands.processors.HelpCmdProcessor;
import io.vertx.core.eventbus.EventBus;

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
    CMD_HELP("help", new HelpCmdProcessor(), "show holistic executive command."),
    CMD_CLEAR("clear", new ClearCmdProcessor(), "clear the screen."),
    CMD_EXIT("exit", new ExitCmdProcessor(), "exit ssh terminal."),
    CMD_CONC("conc", new ConcCmdProcessor(), "test ssh connection."),
    CMD_NUMBER("", null, "select the connection properties.");

    private final String cmd;
    private final AbstractCommandProcessor commandProcessor;
    private final String comment;

    OutsideCommand(String cmd, AbstractCommandProcessor commandProcessor, String comment) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
    }

    public static boolean isOutsideCommand(String cmd) {
        for (OutsideCommand command : values()) {
            if (command.cmd.equals(cmd)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<OutsideCommand> cmdOf(String cmd) {
        OutsideCommand outsideCommand = null;
        for (OutsideCommand command : values()) {
            if (command.cmd.equals(cmd)) {
                outsideCommand = command;
            }
        }
        return Optional.ofNullable(outsideCommand);
    }

    public <T> void processCmd(EventBus eventBus, T param) throws Exception {
        if (this.commandProcessor == null) {
            return;
        }
        this.commandProcessor.process(eventBus, param);
    }

    public String cmd() {
        return cmd;
    }

    public static void printHelp() {
        System.out.println();
        System.out.println("ssh terminal commands: ");
        for (OutsideCommand command : values()) {
            System.out.println("\t" + command.cmd + "\t\t-- " + command.comment);
        }
        System.out.println();
    }
}
