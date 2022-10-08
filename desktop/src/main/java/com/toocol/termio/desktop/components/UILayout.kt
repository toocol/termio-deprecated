@file:JvmName("UILayout")
package com.toocol.termio.desktop.components

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/10/8 11:24
 */
private var invokeOnce: Boolean = false

fun loadLayout() {
    if (invokeOnce) return
    invokeOnce = true

    majorPanel.apply {
        top = topMenuPanel

        center = centerPanel.apply {
            children.addAll(
                workspacePanel.apply {
                    children.addAll(homepage, nativeTerminalEmulator)
                },
                commandExecutor
            )
        }

        left = leftSidePanel.apply {
            left = leftToolSidebar
            center = sessionManageSidebar
        }

        bottom = bottomStatusBar.apply {

        }
    }
}