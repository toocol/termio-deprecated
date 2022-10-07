package com.toocol.termio.desktop.bootstrap

import com.toocol.termio.core.config.core.TermThemeConfigure
import com.toocol.termio.desktop.components.panel.listeners.SshSessionClosedSyncListener
import com.toocol.termio.desktop.components.panel.listeners.SshSessionEstablishedSyncListener
import com.toocol.termio.desktop.components.terminal.config.TerminalConfigure
import com.toocol.termio.platform.nativefx.listeners.WindowResizeEndSyncListener
import com.toocol.termio.platform.nativefx.listeners.WindowResizeStartSyncListener
import com.toocol.termio.platform.nativefx.listeners.WindowResizingSyncListener
import com.toocol.termio.utilities.config.ConfigInstance
import com.toocol.termio.utilities.config.Configure
import com.toocol.termio.utilities.config.ConfigureRegister
import com.toocol.termio.utilities.event.core.AbstractEvent
import com.toocol.termio.utilities.event.core.EventListener
import com.toocol.termio.utilities.event.core.ListenerRegister

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/7 12:01
 * @version: 0.0.1
 */
object TermioCommunityListenerRegister : ListenerRegister() {
    override fun listeners(): Array<out EventListener<out AbstractEvent>> {
        return arrayOf(
            SshSessionEstablishedSyncListener,
            SshSessionClosedSyncListener,
            WindowResizeStartSyncListener,
            WindowResizeEndSyncListener,
            WindowResizingSyncListener
        )
    }
}

object TermioCommunityConfigureRegister : ConfigureRegister() {
    override fun configures(): Array<out Configure<out ConfigInstance>> {
        return arrayOf(
            TermThemeConfigure,
            TerminalConfigure
        )
    }
}