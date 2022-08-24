package com.toocol.termio.desktop.components.homepage.ui

import com.toocol.termio.desktop.components.panel.ui.MajorPanel
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

        val majorPanel = findComponent(MajorPanel::class.java, 1)
        prefWidthProperty().bind(majorPanel.widthProperty().multiply(0.85))
        prefHeightProperty().bind(majorPanel.heightProperty().multiply(0.8))

        val text = Text("This is Homepage.")
        text.fill = Paint.valueOf("#CCCCCC")
        text.layoutX = 10.0
        text.layoutY = 20.0
        children.add(text)
    }

    override fun actionAfterShow() {}
}