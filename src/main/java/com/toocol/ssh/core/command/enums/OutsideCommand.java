package com.toocol.ssh.core.command.enums;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/22 13:21
 */
public class OutsideCommand {

    public static final String CMD_SHOW = "show";

    public static boolean isOutsideCommand(String cmd) {
        if (CMD_SHOW.equals(cmd)) {
            return true;
        }
        return false;
    }
}
