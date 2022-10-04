package com.toocol.termio.utilities.event.api

import com.toocol.termio.utilities.event.core.AbstractEvent
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.utilities.event.core.EventListenerContainer
import com.toocol.termio.utilities.event.core.SyncEvent

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:10
 * @version: 0.0.1
 */
object SyncEventApi {
    fun handle(event: SyncEvent) {
        EventListenerContainer.getListeners(event::class)?.forEach{ listener: EventListener<out AbstractEvent> -> listener.actOn(event.`as`())}
    }
}