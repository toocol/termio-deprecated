package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.geometry.Point2D
import javafx.scene.input.InputMethodRequests
import javafx.scene.layout.Pane
import org.fxmisc.richtext.Caret
import org.fxmisc.richtext.StyleClassedTextField

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 23:31
 * @version: 0.0.1
 */
class CommandExecutorInput(private val id: Long) : StyleClassedTextField(), IStyleAble, IComponent {
    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-input"
        )
    }

    override fun initialize() {
        styled()
        isEditable = true
        showCaret = Caret.CaretVisibility.ON
        inputMethodRequests = InputMethodRequestsObject()
    }

    override fun id(): Long {
        return id
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }
    }

    private class InputMethodRequestsObject : InputMethodRequests {
        override fun getSelectedText(): String {
            return ""
        }

        override fun getLocationOffset(x: Int, y: Int): Int {
            return 0
        }

        override fun cancelLatestCommittedText() {}
        override fun getTextLocation(offset: Int): Point2D {
            return Point2D(0.0, 0.0)
        }
    }
}