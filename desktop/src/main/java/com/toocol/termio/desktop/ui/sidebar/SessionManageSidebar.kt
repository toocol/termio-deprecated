package com.toocol.termio.desktop.ui.sidebar

import com.toocol.termio.platform.ui.TAnchorPane
import com.toocol.termio.desktop.ui.panel.CentralPanel
import com.toocol.termio.desktop.ui.terminal.DesktopTerminalPanel
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:26
 */
class SessionManageSidebar(id: Long) : TAnchorPane(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "session-manage-sidebar"
        )
    }

    override fun initialize() {
        val centralPanel = findComponent(CentralPanel::class.java, 1)
        styled()
        prefHeightProperty().bind(centralPanel.heightProperty())
        prefWidthProperty().bind(centralPanel.widthProperty().multiply(0.15))
        onMouseClicked = EventHandler {
            hide()
            findComponent(DesktopTerminalPanel::class.java, 1).prefWidthProperty().bind(centralPanel.widthProperty())
        }
        centralPanel.left = this
    }

    override fun actionAfterShow() {}
}