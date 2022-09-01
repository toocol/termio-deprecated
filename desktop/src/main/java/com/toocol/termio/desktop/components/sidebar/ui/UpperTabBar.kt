package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.ui.THBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 22:40
 * @version: 0.0.1
 */
class UpperTabBar(id: Long) : THBox(id){
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "upper-tab-bar"
        )
    }

    override fun initialize() {
        run {
            styled()
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        prefHeight = 40.0
        maxHeight = 40.0
        minHeight = 40.0
    }


    override fun actionAfterShow() {}
}