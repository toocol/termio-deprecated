package com.toocol.ssh.core.auth;

import com.toocol.ssh.utilities.address.IAddress;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
public enum AuthAddress implements IAddress {
    /**
     * add ssh credential.
     */
    ADD_CREDENTIAL("ssh.add.credential"),
    /**
     * delete ssh credential.
     */
    DELETE_CREDENTIAL("ssh.delete.credential");

    /**
     * the address string of message
     */
    private final String address;

    AuthAddress(String address) {
        this.address = address;
    }

    @Override
    public String address() {
        return address;
    }
}
