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
     * monitor the terminal program's status
     * include terminal's size and memory use.
     */
    MONITOR_TERMINAL("ssh.term.listen.size.change"),
    /**
     * check typewriting status.
     */
    CHECK_TYPEWRITING_STATUS("ssh.term.check.typewriting")
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
