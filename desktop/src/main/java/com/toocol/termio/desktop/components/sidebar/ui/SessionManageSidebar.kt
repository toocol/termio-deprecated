package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.ui.TAnchorPane
import javafx.scene.layout.Pane

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
        styled()
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        prefWidth = fixedWidth
    }

    override fun actionAfterShow() {}

    companion object {
        var fixedWidth = 180.0
    }
}