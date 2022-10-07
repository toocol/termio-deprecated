package com.toocol.termio.utilities.event.module

import com.toocol.termio.utilities.event.core.AbstractEvent
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.utilities.event.core.EventListenerContainer
import com.toocol.termio.utilities.event.core.ListenerRegister
import com.toocol.termio.utilities.module.ScopeModule
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:02
 * @version: 0.0.1
 */
object EventScopeModule : ScopeModule() {
    @DelicateCoroutinesApi
    override suspend fun start() {
        val listeners: MutableList<out EventListener<out AbstractEvent>> = mutableListOf()
        ListenerRegister.storage.forEach {
            for (li in it.listeners()) listeners.add(li.`as`())
        }
        EventListenerContainer.init(listeners.toTypedArray())
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}