package com.toocol.termio.core.mosh.handlers

import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.mosh.MoshAddress
import com.toocol.termio.core.mosh.core.MoshSession
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.module.NonBlockingMessageHandler
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/30 19:24
 * @version: 0.0.1
 */
class SocketListenHandler(vertx: Vertx?, context: Context?) : NonBlockingMessageHandler(
    vertx!!, context!!) {

    override fun <T> handleInline(message: Message<T>) {
        Optional.ofNullable(MoshSessionCache[cast(message.body())])
            .ifPresent { moshSession: MoshSession -> moshSession.connect(message) }
    }

    override fun consume(): IAddress {
        return MoshAddress.LISTEN_LOCAL_SOCKET
    }
}