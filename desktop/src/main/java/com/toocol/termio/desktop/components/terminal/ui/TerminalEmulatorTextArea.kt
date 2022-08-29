package com.toocol.termio.desktop.components.terminal.ui

import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.component.IStyleAble
import com.toocol.termio.platform.text.EscapedTextStyleClassArea
import com.toocol.termio.platform.text.TextStyle
import com.toocol.termio.utilities.utils.Castable
import javafx.geometry.Point2D
import javafx.scene.input.InputMethodRequests
import javafx.scene.layout.Pane
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
class TerminalEmulatorTextArea(id: Long) : EscapedTextStyleClassArea(id), Castable, IComponent, IStyleAble {

    override fun styleClasses(): Array<String> {
        return arrayOf(
            "console-text-area"
        )
    }

    override fun followCaret(): Boolean {
        return true
    }

    override fun initialize() {
        super.initialize()
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
    }

    override fun sizePropertyBind(major: Pane, widthRatio: Double?, heightRatio: Double?) {
        widthRatio?.run { prefWidthProperty().bind(major.widthProperty().multiply(widthRatio)) }
        heightRatio?.run { prefHeightProperty().bind(major.heightProperty().multiply(heightRatio)) }
    }

    override fun actionAfterShow() {}

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