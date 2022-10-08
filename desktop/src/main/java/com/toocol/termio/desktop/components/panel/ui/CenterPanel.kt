package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.commandExecutor
import com.toocol.termio.desktop.components.workspacePanel
import com.toocol.termio.platform.ui.TVBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/23 12:46
 * @version: 0.0.1
 */
class CenterPanel(id: Long) : TVBox(id) {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "center-panel"
        )
    }

    override fun initialize() {
        styled()
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        workspacePanel.sizePropertyBind(major, widthRatio, heightRatio)
        commandExecutor.sizePropertyBind(major, widthRatio, if (heightRatio == null) null else heightRatio * 0.3)
    }

    override fun actionAfterShow() {
    }
}