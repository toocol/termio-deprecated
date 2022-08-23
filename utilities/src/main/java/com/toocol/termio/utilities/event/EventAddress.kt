package com.toocol.termio.utilities.event

import com.toocol.termio.utilities.module.IAddress

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 15:22
 * @version: 0.0.1
 */
enum class EventAddress(
    // the address string of message
    private val address: String
) : IAddress{
    /**
     * Handle the async event
     */
    HANDLE_ASYNC_EVENT("event.async.handle"),
    ;

    override fun address(): String {
        return address
    }
}