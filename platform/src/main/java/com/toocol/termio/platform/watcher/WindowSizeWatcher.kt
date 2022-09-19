package com.toocol.termio.platform.watcher

import com.toocol.termio.platform.component.IComponent
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.stage.Screen
import javafx.stage.Stage

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/9/19 22:16
 * @version: 0.0.1
 */
class WindowSizeWatcher {
    companion object Instance : IComponent {
        private var isMaximize = false
        private var restoreX = 0.0
        private var restoreY = 0.0
        private var restoreWidth = 0.0
        private var restoreHeight = 0.0

        private const val resizeWidth = 5.00
        private const val minWidth = 400.00
        private const val minHeight = 300.00

        //自定义dialog移动横纵坐标
        private var xOffset = 0.0
        private var yOffset = 0.0
        // 是否处于右边界调整窗口状态
        private var isRight = false
        // 是否处于右下角调整窗口状态
        private var isBottomRight = false
        // 是否处于下边界调整窗口状态
        private var isBottom = false

        private var stage: Stage? = null
        private var root: Node? = null

        @Volatile
        var onMoved = false

        fun init(stage: Stage, root: Node) {
            this.stage = stage
            this.root = root
            initialize()
        }

        override fun initialize() {
            root!!.setOnMouseMoved { event ->
                val x = event.sceneX
                val y = event.sceneY
                val width: Double = stage!!.width
                val height: Double = stage!!.height
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
                root!!.cursor = cursorType
            }

            root!!.setOnMouseDragged { event ->
                //根据鼠标的横纵坐标移动dialog位置
                event.consume()
                if (yOffset != 0.0) {
                    stage!!.x = event.screenX - xOffset
                    if (event.screenY - yOffset < 0) {
                        stage!!.y = 0.0
                    } else {
                        stage!!.y = event.screenY - yOffset
                    }
                }

                val x = event.sceneX
                val y = event.sceneY
                // 保存窗口改变后的x、y坐标和宽度、高度，用于预判是否会小于最小宽度、最小高度
                val nextX: Double = stage!!.x
                val nextY: Double = stage!!.y
                var nextWidth: Double = stage!!.width
                var nextHeight: Double = stage!!.height
                // 所有右边调整窗口状态
                if (isRight || isBottomRight) {
                    nextWidth = x
                }
                // 所有下边调整窗口状态
                if (isBottomRight || isBottom) {
                    nextHeight = y
                }
                // 如果窗口改变后的宽度小于最小宽度，则宽度调整到最小宽度
                if (nextWidth <= minWidth) {
                    nextWidth = minWidth
                }
                // 如果窗口改变后的高度小于最小高度，则高度调整到最小高度
                if (nextHeight <= minHeight) {
                    nextHeight = minHeight
                }
                // 最后统一改变窗口的x、y坐标和宽度、高度，可以防止刷新频繁出现的屏闪情况
                stage!!.x = nextX
                stage!!.y = nextY
                stage!!.width = nextWidth
                stage!!.height = nextHeight
                if (!onMoved) {
                    onMoved = true
                    WindowResizeStartSync.dispatch()
                }
                WindowResizingSync.dispatch()
            }

            root!!.setOnMousePressed { event ->
                event.consume()
                xOffset = event.sceneX
                yOffset = if (event.sceneY > 46) {
                    0.0
                } else {
                    event.sceneY
                }
            }

            root!!.setOnMouseReleased {
                if (onMoved) {
                    onMoved = false
                    WindowResizeEndSync.dispatch()
                }
            }
        }

        fun maximize(): Boolean {
            isMaximize = !isMaximize
            if (isMaximize) {
                val rectangle2d = Screen.getPrimary().visualBounds
                restoreX = stage!!.x
                restoreY = stage!!.y
                restoreWidth = stage!!.width
                restoreHeight = stage!!.height
                stage!!.x = rectangle2d.minX
                stage!!.y = rectangle2d.minY
                stage!!.width = rectangle2d.width
                stage!!.height = rectangle2d.height
            } else {
                stage!!.x = restoreX
                stage!!.y = restoreY
                stage!!.width = restoreWidth
                stage!!.height = restoreHeight
            }
            return isMaximize
        }

        override fun id(): Long {
            return 0
        }

        override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        }
    }
}