package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.sidebar.ui.SessionManageSidebar
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TBorderPane
import com.toocol.termio.platform.ui.TScene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane

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
        styled()

        parser.parse(LeftSidePanel::class.java)
        parser.initializeAll()

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
            val majorPanel = findComponent(MajorPanel::class.java, 1)
            findComponent(WorkspacePanel::class.java, 1).sizePropertyBind(majorPanel, ratio, null)
        }

        center = parser.getAsNode(SessionManageSidebar::class.java)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        parser.getAsComponent(SessionManageSidebar::class.java)?.sizePropertyBind(major, widthRatio, heightRatio)
    }

    override fun actionAfterShow() {
    }
}