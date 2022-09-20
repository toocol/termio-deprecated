package com.toocol.termio.platform.window

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
class WindowSizeAdjuster {
    companion object Instance : IComponent {
        private var isMaximize = false
        private var restoreX = 0.0
        private var restoreY = 0.0
        private var restoreWidth = 0.0
        private var restoreHeight = 0.0

        private const val resizeWidth = 5.00
        private const val minWidth = 400.00
        private const val minHeight = 300.00

        // User defined dialog moves horizontal and vertical coordinates.
        private var xOffset = 0.0
        private var yOffset = 0.0

        // Whether it is in the right border adjustment window state.
        private var isRight = false

        // Whether it is in the lower right corner adjustment window state.
        private var isBottomRight = false

        // Whether it is in the lower boundary adjustment window state.
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
                /*
                 * The mouse cursor is initially the default type.
                 * If it does not enter the adjustment window state, the default type is maintained
                 */
                var cursorType: Cursor = Cursor.DEFAULT

                // Reset the status of all adjustment windows first
                isRight = false.also {
                    isBottom = false
                    isBottomRight = false
                }
                if (y >= height - resizeWidth) {
                    // Adjust the window status in the lower left corner.
                    if (x <= resizeWidth) {
                        // no process
                    } else if (x >= width - resizeWidth) {
                        // Adjust the window status in the lower right corner.
                        isBottomRight = true
                        cursorType = Cursor.SE_RESIZE
                    } else {
                        // Lower boundary adjustment window status
                        isBottom = true
                        cursorType = Cursor.S_RESIZE
                    }
                } else if (x >= width - resizeWidth) {
                    // Right border adjustment window status
                    isRight = true
                    cursorType = Cursor.E_RESIZE
                }
                // Change the mouse cursor
                root!!.cursor = cursorType
            }

            root!!.setOnMouseDragged { event ->
                // Move the dialog position according to the horizontal and vertical coordinates of the mouse.
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

                /*
                 * Save the changed x and y coordinates,
                 * width and height of the window to predict whether it will be smaller than the minimum width and height
                 */
                val nextX: Double = stage!!.x
                val nextY: Double = stage!!.y
                var nextWidth: Double = stage!!.width
                var nextHeight: Double = stage!!.height
                // All right adjustment window states
                if (isRight || isBottomRight) {
                    nextWidth = x
                }
                // Adjust window status of all lower edges
                if (isBottomRight || isBottom) {
                    nextHeight = y
                }
                // If the changed width of the window is less than the minimum width, the width is adjusted to the minimum width
                if (nextWidth <= minWidth) {
                    nextWidth = minWidth
                }
                // If the changed height of the window is less than the minimum height, the height is adjusted to the minimum height
                if (nextHeight <= minHeight) {
                    nextHeight = minHeight
                }
                // Finally, uniformly change the x and y coordinates, width and height of the window to prevent frequent screen flashes
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