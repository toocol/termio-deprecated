package com.toocol.termio.platform.text

import com.google.common.collect.ImmutableMap
import com.toocol.termio.platform.component.IActionAfterShow
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.utilities.escape.*
import com.toocol.termio.utilities.escape.EscapeColorGraphicsMode.*
import com.toocol.termio.utilities.escape.EscapeCommonPrivateMode.*
import com.toocol.termio.utilities.escape.EscapeCursorControlMode.*
import com.toocol.termio.utilities.escape.EscapeEraseFunctionsMode.*
import com.toocol.termio.utilities.escape.EscapeScreenMode.*
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.text.TextFlow
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.StyledTextArea
import org.fxmisc.richtext.TextExt
import org.fxmisc.richtext.model.Codec
import org.fxmisc.richtext.model.SegmentOps
import org.fxmisc.richtext.model.StyledSegment
import org.fxmisc.richtext.model.TwoDimensional
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 17:43
 * @version: 0.0.1
 */
abstract class EscapedTextStyleClassArea(private val id: Long) : GenericStyledArea<ParagraphStyle, String, TextStyle>(
    // default paragraph style
    ParagraphStyle.EMPTY,
    // paragraph style setter
    BiConsumer { paragraph: TextFlow, style: ParagraphStyle ->
        paragraph.style = style.toCss()
    },
    // default segment style
    TextStyle.EMPTY,
    // segment operations
    styledTextOps,
    // Node creator and segment style setter
    Function<StyledSegment<String, TextStyle>, Node> { seg: StyledSegment<String, TextStyle> ->
        createNode(seg) { text: TextExt, style: TextStyle ->
            text.style = style.toCss()
        }
    }
), EscapeCodeSequenceSupporter<EscapedTextStyleClassArea>, IComponent, IActionAfterShow {

    var cursor: Cursor

    protected var paragraphStyle: ParagraphStyle = ParagraphStyle.EMPTY

    @JvmField
    protected var defaultChineseTextStyle: TextStyle = TextStyle.EMPTY
    @JvmField
    protected var defaultEnglishTextStyle: TextStyle = TextStyle.EMPTY

    private var currentChineseTextStyle: TextStyle = TextStyle.EMPTY
    private var currentEnglishTextStyle: TextStyle = TextStyle.EMPTY

    private var ansiEscapeSearchEngine: AnsiEscapeSearchEngine<EscapedTextStyleClassArea>? = null
    private var actionMap: Map<Class<out IEscapeMode>, AnsiEscapeAction<EscapedTextStyleClassArea>>? = null

    fun updateDefaultChineseStyle(style: TextStyle) {
        defaultChineseTextStyle = style
        currentChineseTextStyle = style
    }

    fun updateDefaultEnglishStyle(style: TextStyle) {
        defaultEnglishTextStyle = style
        currentEnglishTextStyle = style
    }

    fun append(text: String?) {
        text ?: return
        ansiEscapeSearchEngine!!.actionOnEscapeMode(text, this)
    }

    fun cursorTest() {
        append("你${StrUtil.NONE_BREAKING_SPACE.repeat(100)}好${StrUtil.NONE_BREAKING_SPACE.repeat(20)}\n")
        append("Hello World~")
        cursor.setTo(calculateCursorInline(0, 100))
        append("不不不不不")
    }

    override fun printOut(text: String) {
        for (ch in text.toCharArray()) {
            val content = if (ch == CharUtil.LF) {
                cursor.setTo(length)
                ch.toString()
            } else ch.toString().replace(StrUtil.SPACE, StrUtil.NONE_BREAKING_SPACE) + CharUtil.INVISIBLE_CHAR

            val start = cursor.inlinePosition
            val end = if (cursor.inlinePosition == length) cursor.inlinePosition else cursor.inlinePosition + content.length
            replace(start, end, content,
                if (StrUtil.isChineseSequenceByHead(content)) currentChineseTextStyle else currentEnglishTextStyle
            )
            if (cursor.inlinePosition != length) {
                cursor.update(2)
            }
        }
    }

    override fun getActionMap(): Map<Class<out IEscapeMode>, AnsiEscapeAction<EscapedTextStyleClassArea>>? {
        return actionMap
    }

    private fun registerActions() {
        val actions: MutableList<AnsiEscapeAction<EscapedTextStyleClassArea>> = ArrayList()
        actions.add(EscapeEnterAction())
        actions.add(EscapeCursorControlAction())
        actions.add(EscapeEraseFunctionsAction())
        actions.add(EscapeColorGraphicsAction())
        actions.add(EscapeColor8To16Action())
        actions.add(EscapeColorISOAction())
        actions.add(EscapeColor256Action())
        actions.add(EscapeColorRgbAction())
        actions.add(EscapeScreenAction())
        actions.add(EscapeCommonPrivateAction())
        actions.add(EscapeKeyBoardStringAction())
        val map: MutableMap<Class<out IEscapeMode>, AnsiEscapeAction<EscapedTextStyleClassArea>> = HashMap()
        for (action in actions) {
            map[action.focusMode()] = action
        }
        actionMap = ImmutableMap.copyOf(map)
    }

    fun calculateCursorInline(line: Int, col: Int): Int {
        return position(line, col).toOffset()
    }

    fun getCursorPos(): Array<Int> {
        val position = offsetToPosition(cursor.inlinePosition, TwoDimensional.Bias.Forward)
        return arrayOf(position.major, position.minor)
    }

    private fun cursorLeft(value: Int) {

    }

    private fun cursorRight(value: Int) {

    }

    private fun cursorUp(value: Int) {

    }

    private fun cursorDown(value: Int) {

    }

    private fun moveCursorLineHead(value: Int) {
        val cursorPos = getCursorPos()
        cursor.setTo(calculateCursorInline(cursorPos[0] + value, 0))
    }

    private fun moveCursorLineUp(value: Int) {

    }

    private fun setColumnTo(value: Int) {

    }

    private class EscapeEnterAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeEnterMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            executeTarget.moveCursorLineHead(0)
        }
    }

    private class EscapeCursorControlAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeCursorControlMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            when (escapeMode) {
                MOVE_CURSOR_TO_CERTAIN -> {
                    val line = params!![0].toString().toInt()
                    val col = params[1].toString().toInt()
                    executeTarget.cursor.setTo(executeTarget.calculateCursorInline(line, col))
                }
                MOVE_HOME_POSITION -> executeTarget.cursor.setTo(0)
                MOVE_CURSOR_UP -> executeTarget.cursorUp(params!![0].toString().toInt())
                MOVE_CURSOR_DOWN -> executeTarget.cursorDown(params!![0].toString().toInt())
                MOVE_CURSOR_LEFT -> executeTarget.cursorLeft(params!![0].toString().toInt())
                MOVE_CURSOR_RIGHT -> executeTarget.cursorRight(params!![0].toString().toInt())
                MOVE_CURSOR_NEXT_LINE_HEAD -> executeTarget.moveCursorLineHead(params!![0].toString().toInt())
                MOVE_CURSOR_PREVIOUS_LINE_HEAD -> executeTarget.moveCursorLineHead(-params!![0].toString().toInt())
                MOVE_CURSOR_TO_COLUMN -> executeTarget.setColumnTo(params!![0].toString().toInt())
                REQUEST_CURSOR_POSITION -> {}
                MOVE_CURSOR_ONE_LINE_UP -> executeTarget.moveCursorLineUp(1)
                SAVE_CURSOR_POSITION_DEC -> {}
                RESTORE_CURSOR_POSITION_DEC -> {}
                SAVE_CURSOR_POSITION_SCO -> {}
                RESTORE_CURSOR_POSITION_SCO -> {}
            }
        }
    }

    private class EscapeEraseFunctionsAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeEraseFunctionsMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            when (escapeMode) {
                ERASE_IN_DISPLAY -> {}
                ERASE_CURSOR_LINE_TO_END -> {}
                ERASE_CURSOR_LINE_TO_BEGINNING -> {}
                ERASE_SCREEN -> executeTarget.clear()
                ERASE_SAVED_LINE -> {}
                ERASE_IN_LINE -> {}
                ERASE_CURSOR_TO_LINE_END -> {}
                ERASE_CURSOR_TO_LINE_START -> {}
                ERASE_LINE -> {}
            }
        }
    }

    private class EscapeColorGraphicsAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeColorGraphicsMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            when (escapeMode) {
                RESET_ALL_MODE -> {
                    executeTarget.currentChineseTextStyle = executeTarget.defaultChineseTextStyle
                    executeTarget.currentEnglishTextStyle = executeTarget.defaultEnglishTextStyle
                }
                BOLD_MODE -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateBold(true)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateBold(true)
                }
                DIM_FAINT_MODE -> {}
                RESET_BOLD_DIM_FAINT -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateBold(false)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateBold(false)
                }
                ITALIC_MODE -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateItalic(true)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateItalic(true)
                }
                RESET_ITALIC -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateItalic(false)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateItalic(false)
                }
                UNDERLINE_MODE -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateUnderline(true)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateUnderline(true)
                }
                RESET_UNDERLINE -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateUnderline(false)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateUnderline(false)
                }
                BLINKING_MODE -> {}
                RESET_BLINKING -> {}
                INVERSE_REVERSE_MODE -> {}
                RESET_INVERSE_REVERSE -> {}
                HIDDEN_VISIBLE_MODE -> {}
                RESET_HIDDEN_VISIBLE -> {}
                STRIKETHROUGH_MODE -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateStrikethrough(true)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateStrikethrough(true)
                }
                RESET_STRIKETHROUGH -> {
                    executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateStrikethrough(false)
                    executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateStrikethrough(false)
                }
            }
        }
    }

    private class EscapeColor8To16Action : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeColor8To16Mode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            val mode = escapeMode as EscapeColor8To16Mode
            val colorHex = mode.hexCode
            if (mode.foreground) {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateTextColor(Color.valueOf(colorHex))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateTextColor(Color.valueOf(colorHex))
            } else {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
            }
        }
    }

    private class EscapeColorISOAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeColorISOMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            val mode = escapeMode as EscapeColorISOMode
            val colorHex = mode.hexCode
            if (mode.foreground) {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateTextColor(Color.valueOf(colorHex))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateTextColor(Color.valueOf(colorHex))
            } else {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
            }
        }
    }

    private class EscapeColor256Action : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeColor256Mode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            val foreground = params!![0].toString().toBoolean()
            val mode = escapeMode as EscapeColor256Mode
            val colorHex = mode.hexCode
            if (foreground) {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateTextColor(Color.valueOf(colorHex))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateTextColor(Color.valueOf(colorHex))
            } else {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
            }
        }
    }

    private class EscapeColorRgbAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeColorRgbMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            val foreground = params!![0].toString().toBoolean()
            val r = params[2].toString().toDouble()
            val g = params[3].toString().toDouble()
            val b = params[4].toString().toDouble()
            if (foreground) {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateTextColor(Color.color(r, g, b))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateTextColor(Color.color(r, g, b))
            } else {
                executeTarget.currentChineseTextStyle = executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.color(r, g, b))
                executeTarget.currentEnglishTextStyle = executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.color(r, g, b))
            }
        }
    }

    private class EscapeScreenAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeScreenMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            when (escapeMode) {
                MONOCHROME_40_25 -> {}
                COLOR_4_40_25 -> {}
                MONOCHROME_80_25 -> {}
                COLOR_4_80_25 -> {}
                COLOR_4_320_200 -> {}
                MONOCHROME_320_200 -> {}
                MONOCHROME_640_200 -> {}
                ENABLE_LINE_WRAPPING -> {}
                COLOR_16_320_200 -> {}
                COLOR_16_640_200 -> {}
                MONOCHROME_640_350 -> {}
                COLOR_16_640_350 -> {}
                MONOCHROME_640_480 -> {}
                COLOR_640_480 -> {}
                COLOR_256_320_200 -> {}
            }
        }
    }

    private class EscapeCommonPrivateAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeCommonPrivateMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            when (escapeMode) {
                CURSOR_INVISIBLE -> {}
                CURSOR_VISIBLE -> {}
                RESTORE_SCREEN -> {}
                SAVE_SCREEN -> {}
                ENABLE_ALTERNATIVE_BUFFER -> {}
                DISABLE_ALTERNATIVE_BUFFER -> {}
            }
        }
    }

    private class EscapeKeyBoardStringAction : AnsiEscapeAction<EscapedTextStyleClassArea>() {
        override fun focusMode(): Class<out IEscapeMode> {
            return EscapeKeyBoardStringMode::class.java
        }

        override fun action(executeTarget: EscapedTextStyleClassArea, escapeMode: IEscapeMode, params: List<Any>?) {
            val code = params!![0]
            val string = params[1]
        }
    }

    init {
        this.setStyleCodecs(
            ParagraphStyle.CODEC,
            Codec.styledSegmentCodec(Codec.STRING_CODEC, TextStyle.CODEC)
        )
        this.registerComponent(id)
        this.registerActions()

        cursor = Cursor(id)
        ansiEscapeSearchEngine = AnsiEscapeSearchEngine()

        textProperty().addListener { _: ObservableValue<out String>?, oldVal: String, newVal: String ->
            cursor.update(newVal.length - oldVal.length)
        }
    }

    override fun id(): Long {
        return id
    }

    companion object {
        private val styledTextOps = SegmentOps.styledTextOps<TextStyle>()
        private fun createNode(
            seg: StyledSegment<String, TextStyle>,
            applyStyle: BiConsumer<in TextExt, TextStyle>,
        ): Node {
            return StyledTextArea.createStyledTextNode(seg.segment, seg.style, applyStyle)
        }
    }
}