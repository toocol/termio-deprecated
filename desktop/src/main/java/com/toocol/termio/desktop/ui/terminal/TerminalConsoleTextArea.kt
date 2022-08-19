package com.toocol.termio.desktop.ui.terminal

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.platform.text.TextStyle
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import javafx.beans.value.ObservableValue
import javafx.geometry.Point2D
import javafx.scene.input.InputMethodRequests
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import org.fxmisc.richtext.Caret

/*
 * A logical line may become multiple physical rows when the window size changes:
 * (logical line, physical line)
 *
 *
 * (1,1): ##################################
 * (2,2): xxx
 * | 
 * | When window resize
 * ↓
 * (1,1): #######################
 * (1,2): ###########
 * (2,3): xxx
 *
 *
 * **Each logical line must be strictly separated by \n**
 * 
 * And by the way, the number of line controlled by Ansi Escape Sequence is actually **physical line**.
 * For example, suppose we have such screen output now **(logical line, physical line)**:
 *
 *
 * (1,1): #######################
 * (1,2): ###########
 * (1,3): xxx
 * |
 * | When input `ESC[2,0Ha`
 * ↓
 * (1,1): #######################
 * (1,2): a##########
 * (2,3): xxx
 * | 
 * | When window resize
 * ↓
 * (1,1): #######################a##########
 * (2,2): xxx
 *
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/6 14:06
 */
class TerminalConsoleTextArea(private val id: Long) : EscapedTextStyleClassArea(id), Castable, IComponent, IStyleAble {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "console-text-area"
        )
    }

    override fun initialize() {
        styled()
        isWrapText = true
        isEditable = false
        useInitialStyleForInsertion = true
        showCaret = Caret.CaretVisibility.OFF
        inputMethodRequests = InputMethodRequestsObject()
        updateDefaultChineseStyle(
            TextStyle.EMPTY.updateFontFamily("\"宋体\"").updateTextColor(Color.valueOf("#cccccc")).updateFontSize(9)
        )
        updateDefaultEnglishStyle(
            TextStyle.EMPTY.updateFontFamily("\"Consolas\"").updateTextColor(Color.valueOf("#cccccc")).updateFontSize(10)
        )
        val desktopTerminalPanel = findComponent(DesktopTerminalPanel::class.java, id)
        prefWidthProperty().bind(desktopTerminalPanel.prefWidthProperty().multiply(1))
        prefHeightProperty().bind(desktopTerminalPanel.prefHeightProperty().multiply(0.99))

        /*
         * Prevent auto caret movement when user pressed '←', '→', '↑', '↓', 'Home', 'PgUp', 'PgDn', instead of setting the caret manually
         */
        addEventFilter(KeyEvent.ANY) { event: KeyEvent ->
            when (event.code) {
                KeyCode.LEFT -> {
                    event.consume()
                    if (cursor.inlinePosition > currentLineStartInParargraph) {
                        cursor.moveLeft()
                    }
                }
                KeyCode.RIGHT -> {
                    event.consume()
                    val index = paragraphs.size - 1
                    if (cursor.inlinePosition < getParagraphLength(index)) {
                        cursor.moveRight()
                    }
                }
                KeyCode.END -> {
                    cursor.setTo(length)
                }
                KeyCode.UP, KeyCode.DOWN, KeyCode.PAGE_DOWN, KeyCode.PAGE_UP, KeyCode.HOME -> event.consume()
                else -> {}
            }
        }
        textProperty().addListener { _: ObservableValue<out String>?, oldVal: String, newVal: String ->
            cursor.update(newVal.length - oldVal.length)
        }
    }

    fun append(text: String?) {
        if (StrUtil.isNewLine(text)) {
            cursor.setTo(length)
        }
        for (splitText in StrUtil.splitSequenceByChinese(text)) {
            replace(
                cursor.inlinePosition, cursor.inlinePosition,
                (StrUtil.join(splitText.toCharArray(), CharUtil.INVISIBLE_CHAR) + CharUtil.INVISIBLE_CHAR).replace(
                    StrUtil.SPACE.toRegex(),
                    StrUtil.NONE_BREAKING_SPACE),
                if (StrUtil.isChineseSequenceByHead(splitText)) defaultChineseTextStyle else defaultEnglishTextStyle
            )
        }
    }

    val physicLines: Int
        get() = paragraphs.size

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