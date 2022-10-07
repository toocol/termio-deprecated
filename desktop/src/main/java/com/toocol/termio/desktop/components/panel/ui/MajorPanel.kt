package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.components.sidebar.ui.BottomStatusBar
import com.toocol.termio.platform.component.Component
import com.toocol.termio.platform.component.ComponentsParser
import com.toocol.termio.platform.component.RegisterComponent
import com.toocol.termio.platform.ui.TBorderPane
import javafx.scene.layout.Pane
import jfxtras.styles.jmetro.JMetroStyleClass

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/11 11:13
 */
@RegisterComponent(value = [
    Component(clazz = TopMenuPanel::class, id = 1, initialVisible = true),
    Component(clazz = LeftSidePanel::class, id = 1, initialVisible = true),
    Component(clazz = CenterPanel::class, id = 1, initialVisible = true),
    Component(clazz = BottomStatusBar::class, id = 1, initialVisible = true)
])
class MajorPanel(id: Long) : TBorderPane(id) {

    private val parser: ComponentsParser = ComponentsParser()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "major-panel",
            JMetroStyleClass.BACKGROUND
        )
    }

    override fun initialize() {
        styled()
        setPrefSize(1280.0, 800.0)

        parser.initializeAll()
        sizePropertyBind(this, 1.0, 1.0)

        top = parser.getAsNode(TopMenuPanel::class.java)
        center = parser.getAsNode(CenterPanel::class.java)
        left = parser.getAsNode(LeftSidePanel::class.java)
        bottom = parser.getAsNode(BottomStatusBar::class.java)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        findComponent(TopMenuPanel::class.java, 1).sizePropertyBind(major, widthRatio, null)
        findComponent(CenterPanel::class.java, 1).sizePropertyBind(this, widthRatio, heightRatio!! * 1.0)
        findComponent(LeftSidePanel::class.java, 1).sizePropertyBind(this, null, heightRatio * 1.0)
        findComponent(BottomStatusBar::class.java, 1).sizePropertyBind(this, widthRatio!! * 1, null)
    }

    override fun actionAfterShow() {
    }

    init {
        parser.parse(this::class.java)
    }
}