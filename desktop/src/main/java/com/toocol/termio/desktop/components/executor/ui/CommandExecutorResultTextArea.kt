package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.platform.text.TextStyle
import javafx.scene.layout.Pane
import javafx.scene.paint.Color

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 23:33
 * @version: 0.0.1
 */
class CommandExecutorResultTextArea(private val id: Long) : EscapedTextStyleClassArea(id), IStyleAble, IComponent {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "command-executor-result-text-area"
        )
    }

    override fun initialize() {
        super.initialize()
        styled()
        isWrapText = true
        isEditable = false

        updateDefaultChineseStyle(TextStyle.EMPTY.updateFontFamily("\"宋体\"").updateTextColor(Color.valueOf("#151515"))
            .updateFontSize(9))
        updateDefaultEnglishStyle(TextStyle.EMPTY.updateFontFamily("\"Consolas\"")
            .updateTextColor(Color.valueOf("#151515")).updateFontSize(10))
    }

    override fun actionAfterShow() {}

    override fun followCaret(): Boolean {
        return false
    }

    override fun id(): Long {
        return id
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }
    }
}