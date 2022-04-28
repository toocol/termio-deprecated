package com.toocol.ssh.core.mosh;

import com.toocol.ssh.utilities.address.IAddress;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public enum MoshAddress implements IAddress{
    /**
     * establish the Mosh session
     */
    ESTABLISH_MOSH_SESSION("mosh.establish.session"),
    ;

    /**
     * the address string of message
     */
    private final String address;

    MoshAddress(String address) {
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }
}
