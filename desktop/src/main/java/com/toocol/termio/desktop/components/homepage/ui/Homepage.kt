package com.toocol.termio.desktop.components.homepage.ui

import com.toocol.termio.desktop.components.panel.ui.TopMenuPanel
import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.platform.ui.TStackPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.Text

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/12 0:43
 * @version: 0.0.1
 */
class Homepage(id: Long) : TStackPane(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "homepage-panel"
        )
    }

    override fun initialize() {
        styled()

        val text = Text("Termio: SSH/Mosh Terminal")
        text.fill = Paint.valueOf("#CCCCCC")
        text.font = Font("Consolas", 20.0)
        children.add(text)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().subtract(TopMenuPanel.fixedHeight + BottomStatusBar.fixedHeight).multiply(heightRatio)) }
    }

    override fun actionAfterShow() {}
}