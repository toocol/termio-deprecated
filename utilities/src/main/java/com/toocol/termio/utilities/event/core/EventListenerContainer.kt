package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.utils.ClassScanner
import java.util.*
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:17
 * @version: 0.0.1
 */
class EventListenerContainer {
    companion object {
        private const val packageName: String = "com.toocol.termio"

        private val syncListenerMap: MutableMap<KClass<out AbstractEvent>, MutableList<EventListener<out AbstractEvent>>> =
            HashMap()
        private val asyncListenerMap: MutableMap<KClass<out AbstractEvent>, MutableList<EventListener<out AbstractEvent>>> =
            HashMap()

        fun init() {
            ClassScanner(packageName) { clazz ->
                Optional.ofNullable(clazz.superclass)
                    .map { superClz -> superClz == EventListener::class.java }
                    .orElse(false)
            }
                .scan()
                .forEach { listenerClazz ->
                    run {
                        val listener = listenerClazz.getDeclaredConstructor().newInstance() as EventListener<out AbstractEvent>
                        if (listener.watch().java.superclass == SyncEvent::class.java) {
                            val list = syncListenerMap.getOrDefault(listener.watch(), mutableListOf())
                            list.add(listener)
                            syncListenerMap[listener.watch()] = list
                        } else if (listener.watch().java.superclass == AsyncEvent::class.java) {
                            val list = asyncListenerMap.getOrDefault(listener.watch(), mutableListOf())
                            list.add(listener)
                            asyncListenerMap[listener.watch()] = list
                        }
                    }
                }
        }

        fun getListeners(clazz: KClass<*>) : List<EventListener<out AbstractEvent>>? {
            return when (clazz.java.superclass) {
                SyncEvent::class.java -> {
                    syncListenerMap[clazz]?.toList()
                }
                AsyncEvent::class.java -> {
                    asyncListenerMap[clazz]?.toList()
                }
                else -> null
            }
        }
    }
}