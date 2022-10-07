package com.toocol.termio.utilities.event.core

import com.toocol.termio.utilities.log.Loggable
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 16:17
 * @version: 0.0.1
 */
class EventListenerContainer {
    companion object : Loggable {
        private val syncListenerMap: MutableMap<KClass<out AbstractEvent>, MutableList<EventListener<out AbstractEvent>>> =
            HashMap()
        private val asyncListenerMap: MutableMap<KClass<out AbstractEvent>, MutableList<EventListener<out AbstractEvent>>> =
            HashMap()

        fun init(listeners : Array<out EventListener<out AbstractEvent>>) {
            listeners.forEach {
                info("Register listener ${it.javaClass.name} success.")
                if (it.watch().java.superclass == SyncEvent::class.java) {
                    val list = syncListenerMap.getOrDefault(it.watch(), mutableListOf())
                    list.add(it)
                    syncListenerMap[it.watch()] = list
                } else if (it.watch().java.superclass == AsyncEvent::class.java) {
                    val list = asyncListenerMap.getOrDefault(it.watch(), mutableListOf())
                    list.add(it)
                    asyncListenerMap[it.watch()] = list
                }
            }
        }

        fun getListeners(clazz: KClass<*>): List<EventListener<out AbstractEvent>>? {
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