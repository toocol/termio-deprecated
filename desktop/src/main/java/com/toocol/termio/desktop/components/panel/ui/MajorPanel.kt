package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.bottomStatusBar
import com.toocol.termio.desktop.components.centerPanel
import com.toocol.termio.desktop.components.leftSidePanel
import com.toocol.termio.desktop.components.topMenuPanel
import com.toocol.termio.platform.ui.TBorderPane
import javafx.scene.layout.Pane
import jfxtras.styles.jmetro.JMetroStyleClass

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:13
 */
class MajorPanel(id: Long) : TBorderPane(id) {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "major-panel",
            JMetroStyleClass.BACKGROUND
        )
    }

    override fun initialize() {
        styled()
        setPrefSize(1280.0, 800.0)

        sizePropertyBind(this, 1.0, 1.0)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        topMenuPanel.sizePropertyBind(major, widthRatio, null)
        centerPanel.sizePropertyBind(this, widthRatio, heightRatio!! * 1.0)
        leftSidePanel.sizePropertyBind(this, null, heightRatio * 1.0)
        bottomStatusBar.sizePropertyBind(this, widthRatio!! * 1, null)
    }

    override fun actionAfterShow() {
    }
}