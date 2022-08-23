package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.platform.text.TextStyle
import com.toocol.termio.utilities.utils.StrUtil
import javafx.application.Platform
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

        val executorPanel = findComponent(CommandExecutor::class.java, id)
        prefWidthProperty().bind(executorPanel.prefWidthProperty().multiply(1))
        prefHeightProperty().bind(executorPanel.prefHeightProperty().multiply(0.9))

        updateDefaultChineseStyle(TextStyle.EMPTY.updateFontFamily("\"宋体\"").updateTextColor(Color.valueOf("#cccccc"))
            .updateFontSize(9))
        updateDefaultEnglishStyle(TextStyle.EMPTY.updateFontFamily("\"Consolas\"")
            .updateTextColor(Color.valueOf("#cccccc")).updateFontSize(10))
    }

    override fun actionAfterShow() {
        // Solve the problem of flashing screen, there may be other better ways
        append(StrUtil.LF.repeat(50))
        Thread {
            Thread.sleep(100)
            Platform.runLater {
                clear()
            }
        }.start()
    }

    override fun id(): Long {
        return id
    }
}