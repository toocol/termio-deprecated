package com.toocol.termio.desktop.components.panel.listeners

import com.toocol.termio.core.ssh.core.SessionClosedSync
import com.toocol.termio.utilities.event.core.EventListener
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/24 18:33
 * @version: 0.0.1
 */
object SshSessionClosedSyncListener : EventListener<SessionClosedSync>() {
    override fun watch(): KClass<SessionClosedSync> {
        return SessionClosedSync::class
    }

    override fun actOn(event: SessionClosedSync) {

    }
}