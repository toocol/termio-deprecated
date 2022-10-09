package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.ui.TVBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 23:22
 * @version: 0.0.1
 */
class LeftToolSidebar : TVBox() {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "left-tool-sidebar"
        )
    }

    override fun initialize() {
        run {
            styled()
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }
        prefWidth = 40.0
        maxWidth = 40.0
        minWidth = 40.0
    }

    override fun actionAfterShow() {}
}