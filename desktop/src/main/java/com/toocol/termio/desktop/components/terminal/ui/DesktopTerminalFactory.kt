package com.toocol.termio.desktop.components.terminal.ui

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/22 21:27
 * @version: 0.0.1
 */
class DesktopTerminalFactory {
    private val instances: MutableMap<Long, WeakReference<DesktopTerminal>> = ConcurrentHashMap()
    private val idMap: MutableMap<Long, Long> = ConcurrentHashMap()

    fun create(id: Long, sessionId: Long): DesktopTerminal {
        val instance = instances.getOrDefault(id, WeakReference(DesktopTerminal(id, sessionId)))
        instances[id] = instance
        idMap[sessionId] = id
        return instance.get()!!
    }

    fun getBySessionId(sessionId: Long): DesktopTerminal? {
        return instances[idMap[sessionId]]?.get()
    }

    fun getById(id: Long): DesktopTerminal? {
        return instances[id]?.get()
    }

    fun release(id: Long) {
        instances.remove(id)
    }
}