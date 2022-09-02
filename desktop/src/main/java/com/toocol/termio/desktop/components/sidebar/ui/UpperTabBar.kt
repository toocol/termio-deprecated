package com.toocol.termio.desktop.components.sidebar.ui

import com.toocol.termio.platform.font.FluentIcon
import com.toocol.termio.platform.ui.TAnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 22:40
 * @version: 0.0.1
 */
class UpperTabBar(id: Long) : TAnchorPane(id){

    private val tabHBox: HBox = HBox()
    private val controlHBox: HBox = HBox()

    private val minimize = Pane()
    private val maximizeOrRestore = Pane()
    private val close = Pane()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "upper-tab-bar"
        )
    }

    override fun initialize() {
        run {
            styled()
            children.addAll(tabHBox, controlHBox)
        }

        tabHBox.run {
            styleClass.add("hbox")
        }

        controlHBox.run {
            styleClass.add("hbox")

            children.addAll(minimize, maximizeOrRestore, close)
        }

        minimize.run {
            styleClass.add("pane-normal")
//            val minimizeIcon = FontAwesomeIcon("\uf068", FontAwesomeIcon.Type.SOLID)
            val minimizeIcon = FluentIcon("\uE921")
            minimizeIcon.setSize(40.0 * 0.7)
            children.add(minimizeIcon)
        }

        maximizeOrRestore.run {
            styleClass.add("pane-normal")
//            val maximizeIcon = FontAwesomeIcon("\uf0c8", FontAwesomeIcon.Type.REGULAR)
            val maximizeIcon = FluentIcon("\uE922")
            maximizeIcon.setSize(40.0 * 0.7)

//            val restoreIcon = FontAwesomeIcon("\uf24d", FontAwesomeIcon.Type.REGULAR);
            val restoreIcon = FluentIcon("\uE923");
            restoreIcon.setSize(40.0 * 0.7)
            restoreIcon.visibleProperty().set(false)

            children.addAll(maximizeIcon, restoreIcon)
        }

        close.run {
            styleClass.add("pane-close")
//            val closeIcon = FontAwesomeIcon("\uf00d", FontAwesomeIcon.Type.SOLID)
            val closeIcon = FluentIcon("\uE8BB")
            closeIcon.setSize(40.0 * 0.7)
            children.add(closeIcon)
        }

        setRightAnchor(controlHBox, 0.0)
        setLeftAnchor(tabHBox, 0.0)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        prefHeight = 40.0
        maxHeight = 40.0
        minHeight = 40.0
    }


    override fun actionAfterShow() {}
}