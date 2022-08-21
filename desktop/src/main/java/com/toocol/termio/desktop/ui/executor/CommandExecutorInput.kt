package com.toocol.termio.desktop.ui.executor

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import javafx.geometry.Point2D
import javafx.scene.input.InputMethodRequests
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
        val executorPanel = findComponent(CommandExecutorPanel::class.java, id)
        executorPanel.top = this
        inputMethodRequests = InputMethodRequestsObject()
        prefWidthProperty().bind(executorPanel.prefWidthProperty().multiply(1))
        prefHeightProperty().bind(executorPanel.prefHeightProperty().multiply(0.15))
    }

    override fun id(): Long {
        return id
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