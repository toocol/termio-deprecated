package com.toocol.ssh.core.command;

import com.toocol.ssh.common.address.IAddress;
import lombok.AllArgsConstructor;


/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
@AllArgsConstructor
public enum CommandVerticleAddress implements IAddress {
    /**
     * execute outside command
     */
    ADDRESS_EXECUTE_OUTSIDE("ssh.command.execute.outside"),
    /**
     * to accept the user command input
     */
    ADDRESS_ACCEPT_COMMAND("ssh.command.accept");

    /**
     * the address string of message
     */
    private final String address;

    @Override
    public String address() {
        return address;
    }

}