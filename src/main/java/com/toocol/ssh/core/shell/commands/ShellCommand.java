package com.toocol.ssh.core.shell.commands;

import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.shell.commands.processors.*;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    CMD_CLEAR("clear", new ShellClearCmdProcessor(), null),
    ;

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

    public final String processCmd(EventBus eventBus, Promise<Long> promise, Shell shell, AtomicBoolean isBreak, String msg) {
        if (this.commandProcessor == null) {
            return StrUtil.EMPTY;
        }
        String result;
        try {
            if (shell.remoteCmd.get().length() > 0) {
                for (int idx = 0; idx < shell.remoteCmd.get().length(); idx ++) {
                    shell.getOutputStream().write('\u007F');
                }
                shell.getOutputStream().flush();
            }

            result = this.commandProcessor.process(eventBus, promise, shell, isBreak, msg);

            shell.getOutputStream().write(StrUtil.LF.getBytes(StandardCharsets.UTF_8));
            shell.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
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
