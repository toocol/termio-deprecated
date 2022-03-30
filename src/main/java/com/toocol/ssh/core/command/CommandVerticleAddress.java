package com.toocol.ssh.core.command;

import com.toocol.ssh.common.router.IAddress;
import lombok.AllArgsConstructor;

import java.util.Optional;

import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_SCREEN_HAS_CLEARED;


/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
@AllArgsConstructor
public enum CommandVerticleAddress implements IAddress {
    /**
     * to clean the screen
     */
    ADDRESS_CLEAR("ssh.command.clear", ADDRESS_SCREEN_HAS_CLEARED),
    /**
     * to exit the program
     */
    ADDRESS_EXIT("ssh.command.exit", null),
    /**
     * execute shell command
     */
    ADDRESS_EXECUTE_SHELL("ssh.command.execute.shell", ADDRESS_CLEAR),
    /**
     * execute outside command
     */
    ADDRESS_EXECUTE_OUTSIDE("ssh.command.execute.outside", null),
    /**
     * to accept the user command input
     */
    ADDRESS_ACCEPT_COMMAND("ssh.command.accept", ADDRESS_EXECUTE_OUTSIDE),
    /**
     * accept anykey input
     */
    ADDRESS_ACCEPT_ANYKEY("ssh.accept.anykey", null);

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