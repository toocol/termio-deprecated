package com.toocol.termio.desktop.ui.executor

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.platform.text.TextStyle
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
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
        styled()
        isWrapText = true
        isEditable = false
        val executorPanel = findComponent(CommandExecutorPanel::class.java, id)
        prefWidthProperty().bind(executorPanel.prefWidthProperty().multiply(1))
        prefHeightProperty().bind(executorPanel.prefHeightProperty().multiply(0.9))
        updateDefaultChineseStyle(TextStyle.EMPTY.updateFontFamily("\"宋体\"").updateTextColor(Color.valueOf("#cccccc"))
            .updateFontSize(9))
        updateDefaultEnglishStyle(TextStyle.EMPTY.updateFontFamily("\"Consolas\"")
            .updateTextColor(Color.valueOf("#cccccc")).updateFontSize(10))
    }

    fun append(text: String?) {
        if (StrUtil.isNewLine(text)) {
            cursor.setTo(length)
        }
        for (splitText in StrUtil.splitSequenceByChinese(text)) {
            replace(
                caretPosition, caretPosition,
                (StrUtil.join(splitText.toCharArray(), CharUtil.INVISIBLE_CHAR) + CharUtil.INVISIBLE_CHAR).replace(
                    StrUtil.SPACE.toRegex(),
                    StrUtil.NONE_BREAKING_SPACE),
                if (StrUtil.isChineseSequenceByHead(splitText)) defaultChineseTextStyle else defaultEnglishTextStyle
            )
        }
    }

    override fun id(): Long {
        return id
    }
}