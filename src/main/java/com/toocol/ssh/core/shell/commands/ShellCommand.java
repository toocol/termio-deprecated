package com.toocol.ssh.core.shell.commands;

import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.shell.commands.processors.*;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum ShellCommand {
    /**
     * shell's command enums
     */
    CMD_EXIT("exit", new ShellExitCmdProcessor(), "Exit current shell, close ssh connection and destroy connect channel."),
    CMD_HANG("hang", new ShellHangCmdProcessor(), "Will not close the connection, exit shell with connection running in the background."),
    CMD_UF("uf", new ShellUfCmdProcessor(), "Batch upload local files to remote connection."),
    CMD_DF("df", new ShellDfCmdProcessor(), "Batch download remote files to local."),
    CMD_CLEAR("clear", new ShellClearCmdProcessor(), null);

    private final String cmd;
    private final ShellCommandProcessor commandProcessor;
    private final String comment;

    ShellCommand(String cmd, ShellCommandProcessor commandProcessor, String comment) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
        this.comment = comment;
    }

    public static Optional<ShellCommand> cmdOf(String cmd) {
        String originCmd = cmd.trim().replaceAll(" {2,}"," ").split(" ")[0];
        ShellCommand shellCommand = null;
        for (ShellCommand command : values()) {
            if (command.cmd.equals(originCmd)) {
                shellCommand = command;
            }
        }
        return Optional.ofNullable(shellCommand);
    }

    public final String processCmd(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String msg) throws Exception {
        if (this.commandProcessor == null) {
            return "";
        }
        return this.commandProcessor.process(eventBus, promise, sessionId, isBreak, msg);
    }

    public String cmd() {
        return cmd;
    }

    public static void printHelp() {
        Printer.println();
        Printer.println("Shell commands:        [param] means optional param");
        for (ShellCommand command : values()) {
            if (command.comment == null) {
                continue;
            }
            Printer.println("\t" + command.cmd + "\t\t-- " + command.comment);
        }
        Printer.println();
    }
}
