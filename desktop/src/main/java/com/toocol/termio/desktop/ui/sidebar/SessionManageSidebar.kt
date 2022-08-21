package com.toocol.termio.desktop.ui.sidebar

import com.toocol.termio.desktop.ui.panel.CentralPanel
import com.toocol.termio.desktop.ui.panel.WorkspacePanel
import com.toocol.termio.platform.ui.TAnchorPane
import com.toocol.termio.platform.ui.TScene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

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
        val centralPanel = findComponent(CentralPanel::class.java, 1)
        styled()
        prefHeightProperty().bind(centralPanel.heightProperty())
        prefWidthProperty().bind(centralPanel.widthProperty().multiply(0.15))

        val scene = findComponent(TScene::class.java, 1)
        val alt1: KeyCombination = KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN)
        scene.accelerators[alt1] = Runnable {
            val ratio = if (isVisible) {
                hide()
                1.0
            } else {
                show()
                0.85
            }
            findComponent(WorkspacePanel::class.java, 1).prefWidthProperty().bind(centralPanel.widthProperty().multiply(ratio))
        }
        centralPanel.left = this
    }

    override fun actionAfterShow() {}
}