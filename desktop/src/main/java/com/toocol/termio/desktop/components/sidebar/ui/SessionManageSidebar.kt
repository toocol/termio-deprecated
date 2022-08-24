package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.desktop.components.panel.ui.MajorPanel
import com.toocol.termio.platform.ui.TAnchorPane

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
        val majorPanel = findComponent(MajorPanel::class.java, 1)
        prefWidthProperty().bind(majorPanel.widthProperty().multiply(0.15))
        prefHeightProperty().bind(majorPanel.heightProperty())
    }

    override fun actionAfterShow() {}
}