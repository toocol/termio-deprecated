package com.toocol.termio.core.ssh

import com.toocol.termio.utilities.module.IAddress

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
enum class SshAddress(
    // the address string of message
    private val address: String
) : IAddress {

    /**
     * establish the ssh session
     */
    ESTABLISH_SSH_SESSION("ssh.establish.session"),

    /**
     * active the ssh session
     */
    ACTIVE_SSH_SESSION("ssh.active.session");

    override fun address(): String {
        return address
    }
}