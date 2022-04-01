package com.toocol.ssh.core.shell.commands;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import com.toocol.ssh.core.shell.commands.processors.ShellClearCmdProcessor;
import com.toocol.ssh.core.shell.commands.processors.ShellExitCmdProcessor;
import com.toocol.ssh.core.shell.commands.processors.ShellHangCmdProcessor;
import io.vertx.core.eventbus.EventBus;

import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum ShellCommand {
    /**
     * shell command enums
     */
    CMD_EXIT("exit", new ShellExitCmdProcessor(), "exit current shell, close ssh connection and destroy connect channel."),
    CMD_HANG("hang", new ShellHangCmdProcessor(), "will not close the connection, exit shell with connection running on the back."),
    CMD_CLEAR("clear", new ShellClearCmdProcessor(), "clear the screen.");

    private final String cmd;
    private final AbstractCommandProcessor commandProcessor;
    private final String comment;

    ShellCommand(String cmd, AbstractCommandProcessor commandProcessor, String comment) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
    }

    public static boolean isShellCommand(String cmd) {
        for (ShellCommand command : values()) {
            if (command.cmd.equals(cmd)) {
                return true;
            }
        }
        return false;
    }

    public static Optional<ShellCommand> cmdOf(String cmd) {
        ShellCommand outsideCommand = null;
        for (ShellCommand command : values()) {
            if (command.cmd.equals(cmd)) {
                outsideCommand = command;
            }
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
        Printer.println("Shell commands:        [param] means optional param");
        for (ShellCommand command : values()) {
            Printer.println("\t" + command.cmd + "\t\t-- " + command.comment);
        }
        Printer.println();
    }
}
