package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.font.FontAwesomeIcon
import com.toocol.termio.platform.font.MDL2Icon
import com.toocol.termio.platform.ui.TAnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.text.Text

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 21:41
 * @version: 0.0.1
 */
class BottomStatusBar(id: Long) : TAnchorPane(id) {

    private val edition = Pane()
    private val functionHBox = HBox()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "bottom-status-bar"
        )
    }

    override fun initialize() {
        run {
            styled()
            children.addAll(edition, functionHBox)

            setRightAnchor(functionHBox, 5.0)
        }

        edition.run {
            styleClass.add("bottom-status-bar-edition-text")

            val icon = MDL2Icon("\uE7F8")
            icon.setSize(fixedHeight)

            val text = Text("Community Edition")
            text.x = 22.0
            text.y = 13.25

            children.addAll(icon, text)
        }

        functionHBox.run {
            styleClass.add("hbox")

            val terminal = Pane()
            val terminalIcon = FontAwesomeIcon("\uf120", FontAwesomeIcon.Type.SOLID)
            terminalIcon.setSize(fixedHeight, fixedHeight * 1.3, fixedHeight)
            terminal.children.add(terminalIcon)
            terminal.styleClass.add("pane")

            val progress = Pane()
            val progressIcon = FontAwesomeIcon("\uf828", FontAwesomeIcon.Type.SOLID)
            progressIcon.setSize(fixedHeight, fixedHeight * 1.3, fixedHeight)
            progress.children.add(progressIcon)
            progress.styleClass.add("pane")

            children.addAll(terminal, progress)
        }
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty()) }
        prefHeight = fixedHeight
        maxHeight = fixedHeight
        minHeight = fixedHeight

        edition.prefHeight = fixedHeight
        edition.maxHeight = fixedHeight
        edition.minHeight = fixedHeight
    }

    override fun actionAfterShow() {}

    companion object {
        @JvmStatic
        val fixedHeight = 17.5
    }
}