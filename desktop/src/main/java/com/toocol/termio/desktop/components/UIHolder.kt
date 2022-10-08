@file:JvmName("UIHolder")
package com.toocol.termio.desktop.components

import com.toocol.termio.desktop.components.executor.ui.CommandExecutor
import com.toocol.termio.desktop.components.homepage.ui.Homepage
import com.toocol.termio.desktop.components.panel.ui.*
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.desktop.components.sidebar.ui.LeftToolSidebar
import com.toocol.termio.desktop.components.sidebar.ui.SessionManageSidebar
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.window.WindowSizeAdjuster.initialVisible

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/10/8 11:02
 */
val majorPanel = MajorPanel(1).initialVisible(true)

val topMenuPanel = TopMenuPanel(1).initialVisible(true)

val leftSidePanel = LeftSidePanel(1).initialVisible(true)
val leftToolSidebar = LeftToolSidebar(1).initialVisible(true)
val sessionManageSidebar = SessionManageSidebar(1).initialVisible(true)

val centerPanel = CenterPanel(1).initialVisible(true)
val workspacePanel = WorkspacePanel(1).initialVisible(true)
val homepage = Homepage(1).initialVisible(true)
val commandExecutor = CommandExecutor(1).initialVisible(true)

val bottomStatusBar = BottomStatusBar(1).initialVisible(true)

fun allUIComponents(): Array<out IComponent> {
    return arrayOf(
        majorPanel,
        topMenuPanel,
        leftSidePanel,
        leftToolSidebar,
        sessionManageSidebar,
        centerPanel,
        workspacePanel,
        homepage,
        commandExecutor,
        bottomStatusBar
    )
}
