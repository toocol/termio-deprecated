package com.toocol.ssh.core.ssh;

import com.toocol.ssh.common.address.IAddress;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public enum SshAddress implements IAddress{
    /**
     * establish the ssh session
     */
    ESTABLISH_SSH_SESSION("ssh.establish.session"),
    /**
     * active the ssh session
     */
    ACTIVE_SSH_SESSION("ssh.active.session")
    ;

    /**
     * the address string of message
     */
    private final String address;

    SshAddress(String address) {
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }
}
