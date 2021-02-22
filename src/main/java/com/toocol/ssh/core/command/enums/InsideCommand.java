package com.toocol.ssh.core.command.enums;

/**
 * system inside command to shell executed.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 9:43
 */
public enum InsideCommand {
    /**
     * Open a new Git-Bash window to execute OpenSSH
     */
    NEW_WINDOW_OPENSSH("'/D/ZhaoZhe/software/Git/git-bash.exe' /f/ssh_terminal_starter/openssh.sh root 47.108.157.178")
    ;
    public String command;

    InsideCommand(String command) {
        this.command = command;
    }
}
