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

        center = parser.getAsNode(CenterPanel::class.java)
        left = parser.getAsNode(LeftSidePanel::class.java)
        bottom = parser.getAsNode(BottomStatusBar::class.java)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        parser.getAsComponent(CenterPanel::class.java)?.sizePropertyBind(this, widthRatio!! * 0.85, heightRatio!! * 1.0)
        parser.getAsComponent(LeftSidePanel::class.java)?.sizePropertyBind(this, widthRatio!! * 0.15, heightRatio!! * 1.0)
        parser.getAsComponent(BottomStatusBar::class.java)?.sizePropertyBind(this, widthRatio!! * 1, null)
    }

    init {
        parser.parse(MajorPanel::class.java)
    }

    override fun actionAfterShow() {}
}