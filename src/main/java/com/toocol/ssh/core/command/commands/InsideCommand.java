package com.toocol.ssh.core.command.commands;

import com.toocol.ssh.core.configuration.vert.ConfigurationVerticle;

/**
 * system inside command to shell executed.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 9:43
 */
public class InsideCommand {

    public static String insideCommandOf(OutsideCommand outsideCommand) {
        switch (outsideCommand) {
            case CMD_SHOW:
                return newWindowOpenssh();
            case CMD_EXIT:
            default:
                return null;
        }
    }

    /**
     * Open a new Git-Bash window to execute OpenSSH
     */
    private static String newWindowOpenssh() {
        return ConfigurationVerticle.SHELL_EXECUTE_OPENSSH_DIR + " " + ConfigurationVerticle.SCRIPT_SSH_DIR + " root 47.108.157.178";
    }

    private InsideCommand() {
    }
}
