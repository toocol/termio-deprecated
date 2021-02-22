package com.toocol.ssh.core.command.enums;

import com.toocol.ssh.core.configuration.vert.ConfigurationVerticle;

/**
 * system inside command to shell executed.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 9:43
 */
public class InsideCommand {
    /**
     * Open a new Git-Bash window to execute OpenSSH
     */
    public static String NEW_WINDOW_OPENSSH() {
        return "'"+ ConfigurationVerticle.GIT_BASH_DIR +"' /F/workspace/github/ssh_terminal_starter/openssh.sh root 47.108.157.178";
    }
    public String command;

    InsideCommand(String command) {
        this.command = command;
    }
}
