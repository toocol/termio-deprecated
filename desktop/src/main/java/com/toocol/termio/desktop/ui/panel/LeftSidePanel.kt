package com.toocol.termio.desktop.ui.panel

import com.toocol.termio.desktop.ui.sidebar.SessionManageSidebar
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TBorderPane
import com.toocol.termio.platform.ui.TScene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/22 22:17
 * @version: 0.0.1
 */
@RegisterComponent(value = [
    Component(clazz = SessionManageSidebar::class, id = 1, initialVisible = true)
])
class LeftSidePanel(id: Long) : TBorderPane(id){
    private val parser: ComponentsParser = ComponentsParser()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "left-side-panel"
        )
    }

    override fun initialize() {
        parser.parse(LeftSidePanel::class.java)
        parser.initializeAll()

        styled()

        val centralPanel = findComponent(CentralPanel::class.java, 1)
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

        center = parser.get(SessionManageSidebar::class.java)
    }

    override fun actionAfterShow() {
    }
}