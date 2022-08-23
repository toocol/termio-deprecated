package com.toocol.termio.utilities.event.handlers

import com.toocol.termio.utilities.event.core.AbstractEvent
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.utilities.event.core.EventListenerContainer
import com.toocol.termio.utilities.event.core.SyncEvent
import com.toocol.termio.utilities.functional.Ordered

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 15:20
 * @version: 0.0.1
 */
class SyncEventHandler {

    fun handleInline(event: SyncEvent) {
        EventListenerContainer.getListeners(event::class)?.forEach{listener: EventListener<out AbstractEvent> -> listener.actOn(event.`as`())}
    }

}