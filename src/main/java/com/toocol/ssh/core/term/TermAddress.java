package com.toocol.ssh.core.term;

import com.toocol.ssh.common.address.IAddress;
import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@AllArgsConstructor
public enum TermAddress implements IAddress{
    /**
     * execute outside command
     */
    ADDRESS_EXECUTE_OUTSIDE("ssh.command.execute.outside"),
    /**
     * to accept the user command input
     */
    ADDRESS_ACCEPT_COMMAND("ssh.command.accept"),
    /**
     * listen the terminal's size change.
     */
    LISTEN_TERMINAL_SIZE_CHANGE("ssh.term.listen.size.change")
    ;

    /**
     * the address string of message
     */
    private final String address;

    @Override
    public String address() {
        return address;
    }
}
