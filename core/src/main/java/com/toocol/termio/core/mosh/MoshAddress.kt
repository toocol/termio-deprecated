package com.toocol.termio.core.mosh

import com.toocol.termio.utilities.module.IAddress

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
enum class MoshAddress(
    // the address string of message
    private val address: String
) : IAddress {
    /**
     * establish the Mosh session
     */
    ESTABLISH_MOSH_SESSION("mosh.establish.session"),

    /**
     * mosh time tick
     */
    MOSH_TICK("mosh.tick"),

    /**
     * mosh listen local udp socket packet receive
     */
    LISTEN_LOCAL_SOCKET("mosh.listen.local.socket"),

    /**
     * mosh close local udp socket
     */
    CLOSE_LOCAL_SOCKET("mosh.close.local.socket");

    override fun address(): String {
        return address
    }
}