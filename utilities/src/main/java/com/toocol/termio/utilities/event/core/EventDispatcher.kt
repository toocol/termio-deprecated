package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.event.EventAddress
import com.toocol.termio.utilities.event.handlers.SyncEventHandler
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:50
 * @version: 0.0.1
 */
class EventDispatcher {
    companion object {
        private val eventCodec = EventCodec()
        private val deliveryOptions = DeliveryOptions().setCodecName(eventCodec.name())
        private val syncEventHandler: SyncEventHandler = SyncEventHandler()

        private var eventBus: EventBus? = null

        fun register(eventBus: EventBus) {
            EventDispatcher.eventBus = eventBus
            eventBus.registerCodec(eventCodec)
        }

        fun dispatch(event: AbstractEvent) {
            if (event is SyncEvent) {
                syncEventHandler.handleInline(event)
            } else if (event is AsyncEvent) {
                eventBus!!.send(EventAddress.HANDLE_ASYNC_EVENT.address(), event, deliveryOptions)
            }
        }
    }
}