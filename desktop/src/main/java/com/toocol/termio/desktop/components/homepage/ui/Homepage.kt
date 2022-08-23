package com.toocol.termio.desktop.components.homepage.ui

import com.toocol.termio.desktop.components.panel.ui.WorkspacePanel
import com.toocol.termio.platform.ui.TAnchorPane
import javafx.scene.paint.Paint
import javafx.scene.text.Text

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:43
 * @version: 0.0.1
 */
class Homepage(id: Long) : TAnchorPane(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "homepage-panel"
        )
    }

    override fun initialize() {
        styled()

        val workspacePanel = findComponent(WorkspacePanel::class.java, 1)
        maxHeightProperty().bind(workspacePanel.prefHeightProperty())
        maxWidthProperty().bind(workspacePanel.prefWidthProperty())
        prefHeightProperty().bind(workspacePanel.prefHeightProperty().multiply(0.8))
        prefWidthProperty().bind(workspacePanel.prefWidthProperty())

        val text = Text("This is Homepage.")
        text.fill = Paint.valueOf("#CCCCCC")
        text.layoutX = 10.0
        text.layoutY = 20.0
        children.add(text)
    }

    override fun actionAfterShow() {}
}