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
    NEW_WINDOW_OPENSSH("/D/ZhaoZhe/software/Git/git-bash.exe /f/openssh.sh")
    ;
    public String command;

    InsideCommand(String command) {
        this.command = command;
    }
}
