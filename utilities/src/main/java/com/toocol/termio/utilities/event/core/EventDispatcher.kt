package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.event.api.AsyncEventApi
import com.toocol.termio.utilities.event.api.SyncEventApi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:50
 * @version: 0.0.1
 */
class EventDispatcher {
    companion object {
        fun dispatch(event: AbstractEvent) {
            if (event is SyncEvent) {
                SyncEventApi.handle(event)
            } else if (event is AsyncEvent) {
                AsyncEventApi.handle(event)
            }
        }
    }
}