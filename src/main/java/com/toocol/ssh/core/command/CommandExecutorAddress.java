package com.toocol.ssh.core.command;

import com.toocol.ssh.common.router.IAddress;
import lombok.AllArgsConstructor;

import java.util.Optional;

/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
@AllArgsConstructor
public enum CommandExecutorAddress implements IAddress {
    /**
     * execute shell command
     */
    ADDRESS_EXECUTE_SHELL("ssh.command.execute.shell", null),
    /**
     * execute outside command
     */
    ADDRESS_EXECUTE_OUTSIDE("ssh.command.execute.outside", null);
    /**
     * the address string of message
     */
    public final String address;
    public final IAddress next;

    @Override
    public String address() {
        return address;
    }

    @Override
    public Optional<IAddress> nextAddress() {
        return Optional.ofNullable(next);
    }
}