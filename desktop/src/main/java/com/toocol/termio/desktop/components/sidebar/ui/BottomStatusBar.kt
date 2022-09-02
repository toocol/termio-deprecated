package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.font.MDL2Icon
import com.toocol.termio.platform.ui.TGridPane
import javafx.scene.layout.Pane
import javafx.scene.text.Text

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 21:41
 * @version: 0.0.1
 */
class BottomStatusBar(id: Long) : TGridPane(id) {

    private val edition = Pane()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "bottom-status-bar"
        )
    }

    override fun initialize() {
        run {
            styled()
            addColumn(10, edition)
        }

        edition.run {
            styleClass.add("bottom-status-bar-edition-text")

            val icon = MDL2Icon("\uE7F8")
            icon.setSize(17.5)

            val text = Text("Community Edition")
            text.x = 22.0
            text.y = 13.25

            children.addAll(icon, text)
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty()) }
        prefHeight = 17.5
        maxHeight = 17.5
        minHeight = 17.5

        edition.prefHeight = 17.5
        edition.maxHeight = 17.5
        edition.minHeight = 17.5
    }

    override fun actionAfterShow() {}
}