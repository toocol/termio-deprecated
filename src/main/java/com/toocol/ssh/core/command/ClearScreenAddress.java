package com.toocol.ssh.core.command;

import com.toocol.ssh.common.router.IAddress;
import lombok.AllArgsConstructor;

import java.util.Optional;

/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
@AllArgsConstructor
public enum ClearScreenAddress implements IAddress {
    /**
     * to clean the screen
     */
    ADDRESS_CLEAR("ssh.command.clear", null);

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