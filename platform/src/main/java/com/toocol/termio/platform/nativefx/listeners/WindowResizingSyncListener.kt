package com.toocol.termio.platform.nativefx.listeners

import com.toocol.termio.platform.watcher.WindowResizingSync
import com.toocol.termio.utilities.event.core.EventListener
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/19 23:05
 * @version: 0.0.1
 */
class WindowResizingSyncListener : EventListener<WindowResizingSync>() {
    override fun watch(): KClass<WindowResizingSync> {
        return WindowResizingSync::class
    }

    override fun actOn(event: WindowResizingSync) {
    }
}