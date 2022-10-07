package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.sidebar.ui.LeftToolSidebar
import com.toocol.termio.desktop.components.sidebar.ui.SessionManageSidebar
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
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
@RegisterComponent(value = [
    Component(clazz = LeftToolSidebar::class, id = 1, initialVisible = true),
    Component(clazz = SessionManageSidebar::class, id = 1, initialVisible = true),
])
class LeftSidePanel(id: Long) : TBorderPane(id) {

    private val parser: ComponentsParser = ComponentsParser()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "left-side-panel"
        )
    }

    override fun initialize() {
        styled()

        parser.initializeAll()

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
            findComponent(WorkspacePanel::class.java, 1).sizePropertyBind(findComponent(MajorPanel::class.java, 1),
                1.0,
                null)
        }

        left = parser.getAsNode(LeftToolSidebar::class.java)
        center = parser.getAsNode(SessionManageSidebar::class.java)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }

        findComponent(LeftToolSidebar::class.java, 1).sizePropertyBind(major, null, heightRatio)
        findComponent(SessionManageSidebar::class.java, 1).sizePropertyBind(major, null, heightRatio)
    }

    override fun actionAfterShow() {
    }

    init {
        parser.parse(this::class.java)
    }
}