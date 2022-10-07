package com.toocol.termio.platform.nativefx.listeners

import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.platform.nativefx.NativeNodeContainer
import com.toocol.termio.platform.window.WindowResizeEndSync
import com.toocol.termio.utilities.event.core.EventListener
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/19 23:06
 * @version: 0.0.1
 */
object WindowResizeEndSyncListener : EventListener<WindowResizeEndSync>() {
    override fun watch(): KClass<WindowResizeEndSync> {
        return WindowResizeEndSync::class
    }

    override fun actOn(event: WindowResizeEndSync) {
        NativeNodeContainer.nodes().forEach {
            NativeBinding.requestFocus(it.key, true, System.nanoTime())
            it.updateOnce = true
        }
    }
}