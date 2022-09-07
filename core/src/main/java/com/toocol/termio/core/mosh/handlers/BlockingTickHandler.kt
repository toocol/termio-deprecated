package com.toocol.termio.core.mosh.handlers

import com.toocol.termio.core.cache.MoshSessionCache
import com.toocol.termio.core.mosh.MoshAddress
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/5/14 21:17
 * @version: 0.0.1
 */
class BlockingTickHandler(vertx: Vertx?, context: Context?, parallel: Boolean) : BlockingMessageHandler<Void?>(
    vertx!!, context!!, parallel) {
    override fun consume(): IAddress {
        return MoshAddress.MOSH_TICK
    }

    @Throws(Exception::class)
    override fun <T> handleBlocking(promise: Promise<Void?>, message: Message<T>) {
        val sessionId = cast<Long>(message.body())
        val moshSession = MoshSessionCache[sessionId]
        while (moshSession != null && moshSession.isConnected) {
            moshSession.tick()
            Thread.sleep(1)
        }
        promise.complete()
    }

    @Throws(Exception::class)
    override fun <T> resultBlocking(asyncResult: AsyncResult<Void?>, message: Message<T>) {
    }
}