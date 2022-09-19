package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.bootstrap.StageHolder
import com.toocol.termio.platform.watcher.WindowSizeWatcher
import com.toocol.termio.platform.font.FluentIcon
import com.toocol.termio.platform.ui.TAnchorPane
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane


/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 22:30
 * @version: 0.0.1
 */
class TopMenuPanel(id: Long) : TAnchorPane(id){
    private val tabHBox: HBox = HBox()
    private val controlHBox: HBox = HBox()

    private val minimize = Pane()
    private val maximizeOrRestore = Pane()
    private val close = Pane()

    private val windowSizeWatcher = WindowSizeWatcher.Instance

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "top-menu-panel"
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

        val stage = StageHolder.stage!!
        minimize.run {
            styleClass.add("pane-normal")
            val minimizeIcon = FluentIcon("\uE921")
            minimizeIcon.setSize(fixedHeight, fixedHeight * 1.4, fixedHeight)
            children.add(minimizeIcon)

            setOnMouseClicked { event ->
                if (event.clickCount == 1 && event.button == MouseButton.PRIMARY) {
                    stage.isIconified = true
                }
            }
        }

        maximizeOrRestore.run {
            styleClass.add("pane-normal")
            val maximizeIcon = FluentIcon("\uE922")
            maximizeIcon.setSize(fixedHeight, fixedHeight * 1.4, fixedHeight)

            val restoreIcon = FluentIcon("\uE923")
            restoreIcon.setSize(fixedHeight, fixedHeight * 1.4, fixedHeight)
            restoreIcon.visibleProperty().set(false)

            children.addAll(maximizeIcon, restoreIcon)
            setOnMouseClicked { event ->
                if (event.clickCount == 1 && event.button == MouseButton.PRIMARY) {
                    if (windowSizeWatcher.maximize()) {
                        restoreIcon.visibleProperty().set(true)
                        maximizeIcon.visibleProperty().set(false)
                    } else {
                        restoreIcon.visibleProperty().set(false)
                        maximizeIcon.visibleProperty().set(true)
                    }
                }
            }
        }

        close.run {
            styleClass.add("pane-close")
            val closeIcon = FluentIcon("\uE8BB")
            closeIcon.setSize(fixedHeight, fixedHeight * 1.4, fixedHeight)
            children.add(closeIcon)
            setOnMouseClicked { event ->
                if (event.clickCount == 1 && event.button == MouseButton.PRIMARY) {
                    stage.close()
                }
            }
        }

        setRightAnchor(controlHBox, 0.0)
        setLeftAnchor(tabHBox, 0.0)
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        prefHeight = fixedHeight
        maxHeight = fixedHeight
        minHeight = fixedHeight
    }


    override fun actionAfterShow() {}

    companion object {
        @JvmStatic
        val fixedHeight = 20.0
    }
}