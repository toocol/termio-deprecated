package com.toocol.termio.core.auth

import com.toocol.termio.utilities.module.IAddress

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
enum class AuthAddress(
    // the address string of message
    private val address: String
) : IAddress {

    /**
     * add ssh credential.
     */
    ADD_CREDENTIAL("ssh.add.credential"),

    /**
     * delete ssh credential.
     */
    DELETE_CREDENTIAL("ssh.delete.credential");

    override fun address(): String {
        return address
    }
}