package com.toocol.termio.platform.nativefx.listeners

import com.toocol.termio.platform.nativefx.NativeBinding
import com.toocol.termio.platform.nativefx.NativeNodeContainer
import com.toocol.termio.platform.window.WindowResizingSync
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
        NativeNodeContainer.nodes().forEach {
            NativeBinding.processNativeEvents(it.key)
            val resp = NativeBinding.sendMsg(
                it.key,
                "${NativeBinding.getW(it.key)},${NativeBinding.getH(it.key)}",
                NativeBinding.SharedStringType.NFX_REQUEST_SIZE.type
            )
        }
    }
}