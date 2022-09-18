package com.toocol.termio.desktop.components.terminal.ui

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/22 21:27
 * @version: 0.0.1
 */
class TerminalEmulatorFactory {
    companion object Instance {
        private val instances: MutableMap<Long, WeakReference<NativeTerminalEmulator>> = ConcurrentHashMap()
        private val idMap: MutableMap<Long, Long> = ConcurrentHashMap()

        fun create(id: Long, sessionId: Long): NativeTerminalEmulator {
            val instance = instances.getOrDefault(id, WeakReference(NativeTerminalEmulator(id, sessionId)))
            instances[id] = instance
            idMap[sessionId] = id
            return instance.get()!!
        }

        fun getAllTerminals(): MutableList<NativeTerminalEmulator> {
            val list: MutableList<NativeTerminalEmulator> = mutableListOf()
            instances.values.asSequence()
                .forEach { list.add(it.get()!!) }
            return list
        }

        fun getBySessionId(sessionId: Long): NativeTerminalEmulator? {
            return instances[idMap[sessionId]]?.get()
        }

        fun getById(id: Long): NativeTerminalEmulator? {
            return instances[id]?.get()
        }

        fun release(id: Long) {
            instances.remove(id)
        }
    }
}