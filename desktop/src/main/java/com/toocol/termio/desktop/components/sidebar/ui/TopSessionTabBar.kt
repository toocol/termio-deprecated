package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.ui.THBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/11 22:40
 * @version: 0.0.1
 */
class TopSessionTabBar(id: Long) : THBox(id){
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "top-session-tab-bar"
        )
    }

    override fun initialize() {
        styled()
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        prefHeight = fixedHeight
        maxHeight = fixedHeight
        minHeight = fixedHeight
    }

    override fun actionAfterShow() {
    }

    companion object {
        @JvmStatic
        val fixedHeight = 25.0
    }
}