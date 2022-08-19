package com.toocol.termio.desktop.ui.panel

import com.toocol.termio.platform.ui.TBorderPane
import com.toocol.termio.desktop.ui.panel.CentralPanel

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:39
 * @version: 0.0.1
 */
class WorkspacePanel(id: Long) : TBorderPane(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "workspace-panel"
        )
    }

    override fun initialize() {
        styled()
        val centralPanel = findComponent(CentralPanel::class.java, 1)
        maxHeightProperty().bind(centralPanel.heightProperty())
        maxWidthProperty().bind(centralPanel.widthProperty().multiply(0.85))
        prefHeightProperty().bind(centralPanel.heightProperty())
        prefWidthProperty().bind(centralPanel.widthProperty().multiply(0.85))
        centralPanel.center = this
    }

    override fun actionAfterShow() {}
}