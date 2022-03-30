package com.toocol.ssh.core.command.enums;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public enum OutsideCommand {
    /**
     * outside command enums
     */
    CMD_SHOW("show"),
    CMD_EXIT("exit");

    private final String cmd;

    OutsideCommand(String cmd) {
        this.cmd = cmd;
    }

    public static boolean isOutsideCommand(String cmd) {
        for (OutsideCommand command : values()) {
            if (command.cmd.equals(cmd)) {
                return true;
            }
        }
        return false;
    }

    public String cmd() {
        return cmd;
    }
}
