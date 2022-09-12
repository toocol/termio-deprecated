package com.toocol.termio.desktop.components.panel.ui

import com.toocol.termio.desktop.bootstrap.StageHolder
import com.toocol.termio.platform.font.FluentIcon
import com.toocol.termio.platform.ui.TAnchorPane
import javafx.scene.Cursor
import javafx.scene.input.MouseButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.stage.Screen


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

    private var isMaximize = false
    private var restoreX = 0.0
    private var restoreY = 0.0
    private var restoreWidth = 0.0
    private var restoreHeight = 0.0

    private val resizeWidth = 5.00
    private val MIN_WIDTH = 400.00
    private val MIN_HEIGHT = 300.00
    private var xOffset = 0.0
    private var yOffset = 0.0 //自定义dialog移动横纵坐标
    // 是否处于右边界调整窗口状态
    private var isRight = false
    // 是否处于右下角调整窗口状态
    private var isBottomRight = false
    // 是否处于下边界调整窗口状态
    private var isBottom = false

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
                    isMaximize = !isMaximize
                    if (isMaximize) {
                        val rectangle2d = Screen.getPrimary().visualBounds
                        restoreX = stage.x
                        restoreY = stage.y
                        restoreWidth = stage.width
                        restoreHeight = stage.height
                        stage.x = rectangle2d.minX
                        stage.y = rectangle2d.minY
                        stage.width = rectangle2d.width
                        stage.height = rectangle2d.height
                        restoreIcon.visibleProperty().set(true)
                        maximizeIcon.visibleProperty().set(false)
                    } else {
                        stage.x = restoreX
                        stage.y = restoreY
                        stage.width = restoreWidth
                        stage.height = restoreHeight
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

        val root = findComponent(MajorPanel::class.java, 1)
        root.setOnMouseMoved { event ->
            val x = event.sceneX
            val y = event.sceneY
            val width: Double = stage.width
            val height: Double = stage.height
            var cursorType: Cursor = Cursor.DEFAULT // 鼠标光标初始为默认类型，若未进入调整窗口状态，保持默认类型

            // 先将所有调整窗口状态重置
            isRight = false.also {
                isBottom = false
                isBottomRight = false
            }
            if (y >= height - resizeWidth) {
                // 左下角调整窗口状态
                if (x <= resizeWidth) {
                    // no process
                } else if (x >= width - resizeWidth) {
                    // 右下角调整窗口状态
                    isBottomRight = true
                    cursorType = Cursor.SE_RESIZE
                } else {
                    // 下边界调整窗口状态
                    isBottom = true
                    cursorType = Cursor.S_RESIZE
                }
            } else if (x >= width - resizeWidth) {
                // 右边界调整窗口状态
                isRight = true
                cursorType = Cursor.E_RESIZE
            }
            // 最后改变鼠标光标
            root.cursor = cursorType
        }

        root.setOnMouseDragged { event ->
            //根据鼠标的横纵坐标移动dialog位置
            //根据鼠标的横纵坐标移动dialog位置
            event.consume()
            if (yOffset != 0.0) {
                stage.x = event.screenX - xOffset
                if (event.screenY - yOffset < 0) {
                    stage.y = 0.0
                } else {
                    stage.y = event.screenY - yOffset
                }
            }

            val x = event.sceneX
            val y = event.sceneY
            // 保存窗口改变后的x、y坐标和宽度、高度，用于预判是否会小于最小宽度、最小高度
            val nextX: Double = stage.x
            val nextY: Double = stage.y
            var nextWidth: Double = stage.width
            var nextHeight: Double = stage.height
            // 所有右边调整窗口状态
            if (isRight || isBottomRight) {
                nextWidth = x
            }
            // 所有下边调整窗口状态
            if (isBottomRight || isBottom) {
                nextHeight = y
            }
            // 如果窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
            if (nextWidth <= MIN_WIDTH) {
                nextWidth = MIN_WIDTH
            }
            // 如果窗口改变后的高度小于最小高度，则高度调整到最小高度
            if (nextHeight <= MIN_HEIGHT) {
                nextHeight = MIN_HEIGHT
            }
            // 最后统一改变窗口的x、y坐标和宽度、高度，可以防止刷新频繁出现的屏闪情况
            stage.x = nextX
            stage.y = nextY
            stage.width = nextWidth
            stage.height = nextHeight
        }

        root.setOnMousePressed { event ->
            event.consume()
            xOffset = event.sceneX
            yOffset = if (event.sceneY > 46) {
                0.0
            } else {
                event.sceneY
            }
        }
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
        val fixedHeight = 25.0
    }
}