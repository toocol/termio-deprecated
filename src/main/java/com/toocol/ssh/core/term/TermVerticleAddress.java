package com.toocol.ssh.core.term;

import com.toocol.ssh.common.address.IAddress;
import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@AllArgsConstructor
public enum TermVerticleAddress implements IAddress{
    /**
     * listen the terminal's size change.
     */
    LISTEN_TERMINAL_SIZE_CHANGE("ssh.exec.single.command.in.certain.shell"),
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
