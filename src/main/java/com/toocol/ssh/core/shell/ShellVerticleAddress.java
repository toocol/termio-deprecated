package com.toocol.ssh.core.shell;

import com.toocol.ssh.common.router.IAddress;
import lombok.AllArgsConstructor;

import java.util.Optional;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 20:44
 */
@AllArgsConstructor
public enum ShellVerticleAddress implements IAddress{
    /**
     * execute outside command
     */
    ADDRESS_OPEN_SHELL("ssh.open.shell", null);

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
