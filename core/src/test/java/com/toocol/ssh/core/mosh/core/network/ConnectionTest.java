package com.toocol.ssh.core.mosh.core.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/29 0:55
 * @version:
 */
class ConnectionTest {

    @Test
    void timeout() {
        Transport.Addr addr = new Transport.Addr("localhost", 60001, "WkQ1ElbK11SJywYUggbl7g");
        Connection connection = new Connection(addr, null);
        assertEquals(1000, connection.timeout());
    }

}