package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.ui.THBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 21:41
 * @version: 0.0.1
 */
class BottomStatusBar(id: Long) : THBox(id) {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "bottom-status-bar"
        )
    }

    override fun initialize() {
        styled()
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty()) }
        prefHeight = 17.5
        maxHeight = 17.5
        minHeight = 17.5
    }

    override fun actionAfterShow() {}
}