@file:JvmName("UIHolder")
package com.toocol.termio.desktop.components

import com.toocol.termio.desktop.components.executor.ui.CommandExecutor
import com.toocol.termio.desktop.components.homepage.ui.Homepage
import com.toocol.termio.desktop.components.panel.ui.*
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.desktop.components.sidebar.ui.LeftToolSidebar
import com.toocol.termio.desktop.components.sidebar.ui.SessionManageSidebar
import com.toocol.termio.desktop.components.terminal.ui.NativeTerminalEmulator
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.window.WindowSizeAdjuster.initialVisible

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/10/8 11:02
 */
val majorPanel = MajorPanel().initialVisible(true)

val topMenuPanel = TopMenuPanel().initialVisible(true)

val leftSidePanel = LeftSidePanel().initialVisible(true)
val leftToolSidebar = LeftToolSidebar().initialVisible(true)
val sessionManageSidebar = SessionManageSidebar().initialVisible(true)

val centerPanel = CenterPanel().initialVisible(true)
val workspacePanel = WorkspacePanel().initialVisible(true)
val homepage = Homepage().initialVisible(true)
val nativeTerminalEmulator = NativeTerminalEmulator().initialVisible(false)
val commandExecutor = CommandExecutor().initialVisible(true)

val bottomStatusBar = BottomStatusBar().initialVisible(true)

fun allUIComponents(): Array<out IComponent> {
    return arrayOf(
        majorPanel,
        topMenuPanel,
        leftSidePanel,
        leftToolSidebar,
        sessionManageSidebar,
        centerPanel,
        workspacePanel,
        nativeTerminalEmulator,
        homepage,
        commandExecutor,
        bottomStatusBar
    )
}
