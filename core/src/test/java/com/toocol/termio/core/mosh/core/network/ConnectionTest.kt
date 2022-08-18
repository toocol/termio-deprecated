package com.toocol.termio.core.mosh.core.network

import com.toocol.termio.core.mosh.core.network.Transport.Addr
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/6/29 0:55
 * @version:
 */
internal class ConnectionTest {
    @Test
    fun timeout() {
        val addr = Addr("localhost", 60001, "WkQ1ElbK11SJywYUggbl7g")
        val connection = Connection(addr, null)
        Assertions.assertEquals(1000, connection.timeout())
    }
}