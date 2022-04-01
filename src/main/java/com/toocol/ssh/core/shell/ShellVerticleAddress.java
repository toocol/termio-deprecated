package com.toocol.ssh.core.shell;

import com.toocol.ssh.common.address.IAddress;
import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@AllArgsConstructor
public enum ShellVerticleAddress implements IAddress{
    /**
     * establish a ssh session
     */
    ESTABLISH_SESSION("ssh.establish.session"),
    /**
     * accept the shell cmd
     */
    ACCEPT_SHELL_CMD("ssh.accept.shell.cmd"),
    /**
     * exhibit the shell feedback
     */
    EXHIBIT_SHELL("ssh.exhibit.shell");

    /**
     * the address string of message
     */
    private final String address;

    @Override
    public String address() {
        return address;
    }
}
