package com.toocol.termio.utilities.event.handlers

import com.toocol.termio.utilities.event.EventAddress
import com.toocol.termio.utilities.event.core.AbstractEvent
import com.toocol.termio.utilities.event.core.AsyncEvent
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.utilities.event.core.EventListenerContainer
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.module.NonBlockingMessageHandler
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 15:20
 * @version: 0.0.1
 */
class AsyncEventHandler(vertx: Vertx, context: Context) : NonBlockingMessageHandler(vertx, context) {
    override fun consume(): IAddress {
        return EventAddress.HANDLE_ASYNC_EVENT
    }

    override fun <T> handleInline(message: Message<T>) {
        val event = message.body() as AsyncEvent
        EventListenerContainer.getListeners(event::class)?.forEach { listener: EventListener<out AbstractEvent> ->
            vertx.executeBlocking<Void> { promise ->
                listener.actOn(event.`as`())
                promise.complete()
            }
        }
    }
}