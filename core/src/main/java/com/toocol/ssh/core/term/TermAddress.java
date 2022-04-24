package com.toocol.ssh.core.term;

import com.toocol.ssh.utilities.address.IAddress;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public enum TermAddress implements IAddress{
    /**
     * execute outside command
     */
    EXECUTE_OUTSIDE("ssh.command.execute.outside"),
    /**
     * to accept the user command input
     */
    ACCEPT_COMMAND("ssh.command.accept"),
    /**
     * monitor the terminal program's status
     * include terminal's size and memory use.
     */
    MONITOR_TERMINAL("ssh.term.listen.size.change"),
    /**
     * check typewriting status.
     */
    CHECK_TYPEWRITING_STATUS("ssh.term.check.typewriting"),
    /**
     * deal with the termio command echo and prompt display.
     */
    TERMINAL_ECHO("ssh.term.echo")
    ;

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
