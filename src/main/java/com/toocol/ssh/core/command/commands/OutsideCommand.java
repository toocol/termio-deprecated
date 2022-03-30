package com.toocol.ssh.core.command.commands;

import com.toocol.ssh.core.command.commands.processors.ExecuteExternalShellProcessor;
import com.toocol.ssh.core.command.commands.processors.ExitCmdProcessor;

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
    CMD_HELP("help", null),
    CMD_SHOW("show", new ExecuteExternalShellProcessor()),
    CMD_EXIT("exit", new ExitCmdProcessor()),
    CMD_NUMBER("", null);

    private final String cmd;
    private final AbstractCommandProcessor commandProcessor;

    OutsideCommand(String cmd, AbstractCommandProcessor commandProcessor) {
        this.cmd = cmd;
        this.commandProcessor = commandProcessor;
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

    public <T> void processCmd(T param) {
        if (this.commandProcessor == null) {
            return;
        }
        this.commandProcessor.process(param);
    }

    public String cmd() {
        return cmd;
    }
}
