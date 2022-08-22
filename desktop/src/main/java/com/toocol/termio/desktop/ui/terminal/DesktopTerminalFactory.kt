package com.toocol.termio.desktop.ui.terminal

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/22 21:27
 * @version: 0.0.1
 */
class DesktopTerminalFactory {
    private val instances: MutableMap<Long, WeakReference<DesktopTerminal>> = ConcurrentHashMap()

    fun getInstance(id: Long): DesktopTerminal {
        val instance = instances.getOrDefault(id, WeakReference(DesktopTerminal(id)))
        instances[id] = instance
        return instance.get()!!
    }

    fun release(id: Long) {
        instances.remove(id)
    }
}