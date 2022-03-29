package com.toocol.ssh.common.router;

import java.util.Optional;

/**
 * @author JoeZane (joezane.cn@gmail.com)
 * @date 2022/03/29 16:51:33
 */
public interface IAddress {
    /**
     * return the address string
     *
     * @return Address
     */
    String address();

    /**
     * return the next address string in all program process
     *
     * @return optional
     */
    Optional<IAddress> nextAddress();
}