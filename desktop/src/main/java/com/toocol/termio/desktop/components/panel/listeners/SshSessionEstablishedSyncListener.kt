package com.toocol.termio.desktop.components.panel.listeners

import com.toocol.termio.core.ssh.core.TrySshSessionSync
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.desktop.components.panel.ui.WorkspacePanel
import com.toocol.termio.platform.component.ComponentsContainer
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 20:02
 * @version: 0.0.1
 */
object SshSessionEstablishedSyncListener : EventListener<TrySshSessionSync>() {
    override fun watch(): KClass<TrySshSessionSync> {
        return TrySshSessionSync::class
    }

    override fun actOn(event: TrySshSessionSync) {
        val workspacePanel: WorkspacePanel = ComponentsContainer[WorkspacePanel::class.java, 1]
        workspacePanel.createSshSession(event.sessionId, event.host, event.user, event.password)
    }
}