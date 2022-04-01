package com.toocol.ssh.core.credentials;

import com.toocol.ssh.common.address.IAddress;
import lombok.AllArgsConstructor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@AllArgsConstructor
public enum CredentialVerticleAddress implements IAddress{
    /**
     * add a ssh credential
     */
    ADD_CREDENTIAL("ssh.add.credential");

    /**
     * the address string of message
     */
    private final String address;

    @Override
    public String address() {
        return address;
    }
}
