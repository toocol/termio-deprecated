package com.toocol.termio.desktop.components.panel.listeners

import com.toocol.termio.core.ssh.core.SessionEstablishedSync
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.desktop.components.panel.ui.WorkspacePanel
import com.toocol.termio.platform.component.ComponentsContainer
import kotlin.reflect.KClass

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 20:02
 * @version: 0.0.1
 */
class SshSessionEstablishedSyncListener : EventListener<SessionEstablishedSync>() {
    override fun watch(): KClass<SessionEstablishedSync> {
        return SessionEstablishedSync::class
    }

    override fun actOn(event: SessionEstablishedSync) {
        val workspacePanel: WorkspacePanel = ComponentsContainer[WorkspacePanel::class.java, 1]
        workspacePanel.createTerminal(event.sessionId)
    }
}