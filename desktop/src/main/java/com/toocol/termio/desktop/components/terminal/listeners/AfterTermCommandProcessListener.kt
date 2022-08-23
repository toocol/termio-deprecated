package com.toocol.termio.desktop.components.terminal.listeners

import com.toocol.termio.core.term.commands.AfterTermCommandProcessSyncEvent
import com.toocol.termio.utilities.event.core.EventListener
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 20:02
 * @version: 0.0.1
 */
class AfterTermCommandProcessListener : EventListener<AfterTermCommandProcessSyncEvent>() {
    override fun watch(): KClass<AfterTermCommandProcessSyncEvent> {
        return AfterTermCommandProcessSyncEvent::class
    }

    override fun actOn(event: AfterTermCommandProcessSyncEvent) {
    }
}