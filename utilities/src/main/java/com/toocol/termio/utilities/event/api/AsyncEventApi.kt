package com.toocol.termio.utilities.event.api

import com.toocol.termio.utilities.event.core.AbstractEvent
import com.toocol.termio.utilities.event.core.AsyncEvent
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.utilities.event.core.EventListenerContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:04
 * @version: 0.0.1
 */
object AsyncEventApi : CoroutineScope by MainScope() {
    fun handle(event: AsyncEvent) {
        EventListenerContainer.getListeners(event::class)?.forEach { listener: EventListener<out AbstractEvent> ->
            launch(Dispatchers.Default) {
                listener.actOn(event.`as`())
            }
        }
    }
}