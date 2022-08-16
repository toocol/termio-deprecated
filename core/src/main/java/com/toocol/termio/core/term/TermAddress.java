package com.toocol.termio.core.term;

import com.toocol.termio.utilities.address.IAddress;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public enum TermAddress implements IAddress {
    /**
     * execute console outside command
     */
    EXECUTE_OUTSIDE_CONSOLE("ssh.command.execute.outside.console"),
    /**
     * execute desktop outside command
     */
    EXECUTE_OUTSIDE_DESKTOP("ssh.command.execute.outside.desktop"),
    /**
     * to accept the user command input
     */
    ACCEPT_COMMAND_CONSOLE("ssh.command.accept.console"),
    /**
     * to accept the user command input
     */
    ACCEPT_COMMAND_DESKTOP("ssh.command.accept.desktop"),
    /**
     * monitor the terminal program's status,
     * include terminal's size and ssh/mosh connection status.
     */
    MONITOR_TERMINAL("ssh.term.listen.size.change"),
    /**
     * check typewriting status.
     */
    CHECK_TYPEWRITING_STATUS("ssh.term.check.typewriting"),
    /**
     * deal with the termio command echo and prompt display.
     */
    TERMINAL_ECHO("ssh.term.echo");

    /**
     * the address string of message
     */
    private final String address;

    TermAddress(String address) {
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }
}
