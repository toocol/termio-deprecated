package com.toocol.termio.desktop.components.executor.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.platform.text.TextStyle
import com.toocol.termio.platform.ui.TScene
import com.toocol.termio.utilities.utils.StrUtil
import javafx.application.Platform
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
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
        styled()
        isWrapText = true
        isEditable = false

        val scene = findComponent(TScene::class.java, 1)
        val ctrlT: KeyCombination = KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN)
        scene.accelerators[ctrlT] = Runnable {
//            val pos = getCursorPos()
//            println("line:${pos[0]}, col:${pos[1]}")
//            val paragraph = paragraphs[pos[0] - 1]
//            println(paragraph.text.length)
//            println(paragraph.text.replace(StrUtil.INVISIBLE_CHAR, "").length)
//            println(lineIndex(0, 140))
//            println(lineToParagraph(5))
//            println(paragraphs[lineToParagraph(5)!!].text)
            println(paragraphs[0].text)
            println(paragraphs[1].text)
            println(paragraphs[2].text)
            println(paragraphs[3].text)
        }

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

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }
    }
}