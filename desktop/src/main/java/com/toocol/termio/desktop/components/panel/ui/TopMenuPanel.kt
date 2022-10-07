package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.platform.font.FluentIcon
import com.toocol.termio.platform.ui.TAnchorPane
import com.toocol.termio.platform.window.StageHolder
import com.toocol.termio.platform.window.WindowSizeAdjuster
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane


/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/1 22:30
 * @version: 0.0.1
 */
class TopMenuPanel(id: Long) : TAnchorPane(id){
    private val menuHBox: HBox = HBox()
    private val controlHBox: HBox = HBox()

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "top-menu-panel"
        )
    }

    override fun initialize() {
        run {
            styled()
            children.addAll(menuHBox, controlHBox)
        }

        menuHBox.run {
            styleClass.add("hbox")
        }

        val stage = StageHolder.stage!!
        controlHBox.run {
            val minimize = Pane()
            val maximizeOrRestore = Pane()
            val close = Pane()

            styleClass.add("hbox")
            children.addAll(minimize, maximizeOrRestore, close)

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
                        if (WindowSizeAdjuster.maximize()) {
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
        }

        setRightAnchor(controlHBox, 0.0)
        setLeftAnchor(menuHBox, 0.0)
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