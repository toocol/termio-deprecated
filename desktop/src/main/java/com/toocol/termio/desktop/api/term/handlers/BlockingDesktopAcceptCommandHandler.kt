package com.toocol.termio.desktop.api.term.handlers

import com.toocol.termio.core.cache.STOP_ACCEPT_OUT_COMMAND
import com.toocol.termio.core.term.TermAddress
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.module.BlockingMessageHandler
import com.toocol.termio.utilities.module.IAddress
import io.vertx.core.AsyncResult
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/15 14:25
 */
class BlockingDesktopAcceptCommandHandler(vertx: Vertx?, context: Context?, parallel: Boolean) :
    BlockingMessageHandler<Boolean?>(
        vertx!!, context!!, parallel) {
    override fun consume(): IAddress {
        return TermAddress.ACCEPT_COMMAND
    }

    override fun <T> handleBlocking(promise: Promise<Boolean?>, message: Message<T>) {
        val term = Term.instance
        while (true) {
            val cmd = term.readLine()
            eventBus.send(TermAddress.EXECUTE_OUTSIDE.address(), cmd)
            if (STOP_ACCEPT_OUT_COMMAND) {
                STOP_ACCEPT_OUT_COMMAND = false
                promise.complete(false)
                break
            }
        }
    }

    override fun <T> resultBlocking(asyncResult: AsyncResult<Boolean?>, message: Message<T>) {}
}