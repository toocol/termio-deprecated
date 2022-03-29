package com.toocol.ssh.core.view;

import com.toocol.ssh.common.router.IAddress;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static com.toocol.ssh.core.command.ClearScreenAddress.ADDRESS_CLEAR;
import static com.toocol.ssh.core.command.CommandAcceptorAddress.ADDRESS_ACCEPT_COMMAND;

/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
@AllArgsConstructor
public enum TerminalViewAddress implements IAddress {
    /**
     * when the screen has been cleaned.
     */
    ADDRESS_SCREEN_HAS_CLEARED("ssh.terminal.view", ADDRESS_ACCEPT_COMMAND),

    ADDRESS_LOADING("ssh.loading", ADDRESS_CLEAR),
    ;

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