package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.leftToolSidebar
import com.toocol.termio.desktop.components.majorPanel
import com.toocol.termio.desktop.components.sessionManageSidebar
import com.toocol.termio.desktop.components.workspacePanel
import com.toocol.termio.platform.ui.TBorderPane
import com.toocol.termio.platform.window.StageHolder
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/22 22:17
 * @version: 0.0.1
 */
class LeftSidePanel(id: Long) : TBorderPane(id) {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "left-side-panel"
        )
    }

    override fun initialize() {
        styled()

        val jmetro = JMetro(Style.LIGHT)
        jmetro.parent = this
        styleClass.add(JMetroStyleClass.BACKGROUND)

        val scene = StageHolder.scene!!
        val alt1: KeyCombination = KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN)
        scene.accelerators[alt1] = Runnable {
            if (isVisible) {
                hide()
            } else {
                show()
            }

            workspacePanel.sizePropertyBind(
                majorPanel,
                1.0,
                null
            )
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        leftToolSidebar.sizePropertyBind(major, null, heightRatio)
        sessionManageSidebar.sizePropertyBind(major, null, heightRatio)
    }

    override fun actionAfterShow() {
    }
}