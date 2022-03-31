package com.toocol.ssh.core.ssh;

import com.toocol.ssh.common.router.IAddress;
import lombok.AllArgsConstructor;

import java.util.Optional;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@AllArgsConstructor
public enum SshVerticleAddress implements IAddress{
    /**
     * establish a ssh session
     */
    ESTABLISH_SESSION("ssh.establish.session", null),
    /**
     * connect the channel shell
     */
    CONNECT_CHANNEL_SHELL("ssh.connect.channel.shell", null),
    /**
     * accept the shell cmd
     */
    ACCEPT_SHELL_CMD("ssh.accept.shell.cmd", null),
    /**
     * exhibit the shell feedback
     */
    EXHIBIT_SHELL("ssh.exhibit.shell", null);

    /**
     * the address string of message
     */
    private final String address;
    private final IAddress next;

    @Override
    public String address() {
        return address;
    }

    @Override
    public Optional<IAddress> nextAddress() {
        return Optional.ofNullable(next);
    }
}
