package com.toocol.termio.platform.text

import com.google.common.collect.ImmutableMap
import com.toocol.termio.platform.component.IActionAfterShow
import com.toocol.termio.platform.component.IComponent
import com.toocol.termio.platform.console.TerminalConsolePrintStream
import com.toocol.termio.platform.window.StageHolder
import com.toocol.termio.utilities.escape.*
import com.toocol.termio.utilities.escape.EscapeAsciiControlMode.*
import com.toocol.termio.utilities.escape.EscapeColorGraphicsMode.*
import com.toocol.termio.utilities.escape.EscapeCursorControlMode.*
import com.toocol.termio.utilities.escape.EscapeEraseFunctionsMode.*
import com.toocol.termio.utilities.escape.EscapeOSCMode.*
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import javafx.beans.value.ObservableValue
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import org.fxmisc.richtext.MultiChangeBuilder
import org.fxmisc.richtext.StyleClassedTextArea
import org.fxmisc.richtext.model.ReadOnlyStyledDocument
import org.fxmisc.richtext.model.TwoDimensional
import java.util.concurrent.ConcurrentHashMap


/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/14 17:43
 * @version: 0.0.1
 */
abstract class EscapedStyleClassedArea(private val id: Long) : StyleClassedTextArea(), EscapeCodeSequenceSupporter<EscapedStyleClassedArea>, IComponent, IActionAfterShow {

    private var paragraphStyle: EscapeStyleCollection = EscapeStyleCollection()

    @JvmField
    protected var defaultChineseTextStyle: EscapeStyleCollection = EscapeStyleCollection()

    @JvmField
    protected var defaultEnglishTextStyle: EscapeStyleCollection = EscapeStyleCollection()

    private var currentChineseTextStyle: EscapeStyleCollection = EscapeStyleCollection()
    private var currentEnglishTextStyle: EscapeStyleCollection = EscapeStyleCollection()

    private var paragraphSizeWatch: Int = 0

    private val ansiEscapeSearchEngine: AnsiEscapeSearchEngine<EscapedStyleClassedArea> = AnsiEscapeSearchEngine()
    private val lineIndexParagraphIndexMap: MutableMap<Int, Int> = ConcurrentHashMap()

    private var sizeChanged: Boolean = false
    private var lineIndex: Int = 1
    private var lastParagraph: Int = 0
    private var cursor: Cursor

    fun updateDefaultChineseStyle(style: EscapeStyleCollection) {
        defaultChineseTextStyle = style
        currentChineseTextStyle = style
    }

    fun updateDefaultEnglishStyle(style: EscapeStyleCollection) {
        defaultEnglishTextStyle = style
        currentEnglishTextStyle = style
    }

    /**
     * This method can't invoke in the Javafx Application Thread
     */
    fun append(text: String?, printStream: TerminalConsolePrintStream) {
        text ?: return
        ansiEscapeSearchEngine.actionOnEscapeMode(text, this, printStream)
    }

    abstract fun followCaret(): Boolean

    override fun id(): Long {
        return id
    }

    override fun createMultiChangeBuilder(): MultiChangeBuilder<Collection<String>, String, Collection<String>> {
        return createMultiChange()
    }

    override fun collectReplacement(text: String, multiChangeConst: MultiChangeBuilder<*, *, *>) {
        val multiChange: MultiChangeBuilder<Collection<String>, String, Collection<String>> = cast(multiChangeConst)
        if (text.contains("\n")) {
            val split = text.split("\n")
            split.forEachIndexed { index, str ->
                StrUtil.splitSequenceByChinese(str).forEach {
                    var content = if (it.isNotEmpty()) StrUtil.join(
                        it.toCharArray(),
                        CharUtil.INVISIBLE_CHAR
                    ) + CharUtil.INVISIBLE_CHAR
                    else it
                    content = content.replace(StrUtil.SPACE, StrUtil.NONE_BREAKING_SPACE)

                    val start = if (cursor.inlinePosition <= length) cursor.inlinePosition else length

                    var end = if (cursor.inlinePosition == length) cursor.inlinePosition
                    else if (cursor.inlinePosition < length) cursor.inlinePosition + content.length
                    else length
                    end = if (end > length) length else end

                    multiChange.replace(
                        start, end, ReadOnlyStyledDocument.fromString(
                            content, paragraphStyle,
                            if (StrUtil.isChineseSequenceByHead(content)) currentChineseTextStyle else currentEnglishTextStyle,
                            segOps
                        )
                    )

                    if (cursor.inlinePosition != length) {
                        cursor.update(content.length)
                    }
                }

                if (index != split.size - 1) {
                    multiChange.replace(
                        length, length, ReadOnlyStyledDocument.fromString(
                            StrUtil.LF, paragraphStyle,
                            currentEnglishTextStyle, segOps
                        )
                    )
                    cursor.setTo(length)
                }
            }
        } else {
            StrUtil.splitSequenceByChinese(text).forEach {
                var content = if (it.isNotEmpty()) StrUtil.join(
                    it.toCharArray(),
                    CharUtil.INVISIBLE_CHAR
                ) + CharUtil.INVISIBLE_CHAR
                else it
                content = content.replace(StrUtil.SPACE, StrUtil.NONE_BREAKING_SPACE)

                val start = if (cursor.inlinePosition <= length) cursor.inlinePosition else length

                var end = if (cursor.inlinePosition == length) cursor.inlinePosition
                else if (cursor.inlinePosition < length) cursor.inlinePosition + content.length
                else length
                end = if (end > length) length else end

                multiChange.replace(
                    start, end, ReadOnlyStyledDocument.fromString(
                        content, paragraphStyle,
                        if (StrUtil.isChineseSequenceByHead(content)) currentChineseTextStyle else currentEnglishTextStyle,
                        segOps
                    )
                )
                if (cursor.inlinePosition != length) {
                    cursor.update(content.length)
                }
            }
        }
    }

    override fun getActionMap(): Map<Class<out IEscapeMode>, AnsiEscapeAction<EscapedStyleClassedArea>>? {
        return actionMap
    }

    private fun calculateCursorInline(line: Int, col: Int): Int {
        return position(line, col).toOffset()
    }

    private fun lineToParagraph(line: Int): Int? {
        return lineIndexParagraphIndexMap[line]
    }

    fun getCursorPos(): Array<Int> {
        val pos = offsetToPosition(cursor.inlinePosition, TwoDimensional.Bias.Backward)
        var line = 0
        lineIndexParagraphIndexMap.forEach { (k, v) ->
            if (v == pos.major) {
                line = k
            }
        }
//        return arrayOf(line + lineIndex(pos.major, pos.minor), pos.minor / 2)
        return arrayOf(line, pos.minor / 2)
    }

    fun setCursorTo(row: Int, col: Int) {
        val paragraphIndex = lineToParagraph(row)
        paragraphIndex ?: return
        cursor.setTo(calculateCursorInline(paragraphIndex, col))
    }

    fun cursorLeft(value: Int) {
        cursor.moveLeft(value * 2)
    }

    fun cursorRight(value: Int) {
        cursor.moveRight(value * 2)
    }

    private fun cursorUp(value: Int) {

    }

    private fun cursorDown(value: Int) {

    }

    private fun moveCursorLineHead(value: Int) {
        val cursorPos = getCursorPos()
        setCursorTo(cursorPos[0] + value, 0)
    }

    private fun moveCursorLineUp(value: Int) {

    }

    private fun setColumnTo(value: Int) {

    }

    private class EscapeAsciiControlAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeAsciiControlMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                when (escapeMode) {
                    BEL -> println("System bel")
                    BACKSPACE -> executeTarget.cursorLeft(1)
                    ENTER -> executeTarget.moveCursorLineHead(0)
                }
            }
        }
    }

    private class EscapeCursorControlAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeCursorControlMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                when (escapeMode) {
                    MOVE_CURSOR_TO_CERTAIN -> {
                        val line = params!![0].toString().toInt()
                        val col = params[1].toString().toInt()
                        executeTarget.setCursorTo(line, col * 2)
                    }
                    MOVE_HOME_POSITION -> executeTarget.setCursorTo(0, 0)
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
    }

    private class EscapeEraseFunctionsAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeEraseFunctionsMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
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
    }

    private class EscapeColorGraphicsCombineAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeColorGraphicsCombine::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                if (escapeMode is EscapeColorGraphicsCombine) {
                    escapeMode.list.forEach {
                        actionMap!![it::class.java]!!.action(executeTarget, it, null)
                    }
                }
            }
        }
    }

    private class EscapeColorGraphicsAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeColorGraphicsMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
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
                        executeTarget.currentChineseTextStyle =
                            executeTarget.currentChineseTextStyle.updateItalic(false)
                        executeTarget.currentEnglishTextStyle =
                            executeTarget.currentEnglishTextStyle.updateItalic(false)
                    }
                    UNDERLINE_MODE -> {
                        executeTarget.currentChineseTextStyle =
                            executeTarget.currentChineseTextStyle.updateUnderline(true)
                        executeTarget.currentEnglishTextStyle =
                            executeTarget.currentEnglishTextStyle.updateUnderline(true)
                    }
                    RESET_UNDERLINE -> {
                        executeTarget.currentChineseTextStyle =
                            executeTarget.currentChineseTextStyle.updateUnderline(false)
                        executeTarget.currentEnglishTextStyle =
                            executeTarget.currentEnglishTextStyle.updateUnderline(false)
                    }
                    BLINKING_MODE -> {}
                    RESET_BLINKING -> {}
                    INVERSE_REVERSE_MODE -> {}
                    RESET_INVERSE_REVERSE -> {}
                    HIDDEN_VISIBLE_MODE -> {}
                    RESET_HIDDEN_VISIBLE -> {}
                    STRIKETHROUGH_MODE -> {
                        executeTarget.currentChineseTextStyle =
                            executeTarget.currentChineseTextStyle.updateStrikethrough(true)
                        executeTarget.currentEnglishTextStyle =
                            executeTarget.currentEnglishTextStyle.updateStrikethrough(true)
                    }
                    RESET_STRIKETHROUGH -> {
                        executeTarget.currentChineseTextStyle =
                            executeTarget.currentChineseTextStyle.updateStrikethrough(false)
                        executeTarget.currentEnglishTextStyle =
                            executeTarget.currentEnglishTextStyle.updateStrikethrough(false)
                    }
                }
            }
        }
    }

    private class EscapeColor8To16Action {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeColor8To16Mode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                val mode = escapeMode as EscapeColor8To16Mode
                val colorHex = mode.hexCode
                if (mode.foreground) {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateTextColor(Color.valueOf(colorHex))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateTextColor(Color.valueOf(colorHex))
                } else {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                }
            }
        }
    }

    private class EscapeColorISOAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeColorISOMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                val mode = escapeMode as EscapeColorISOMode
                val colorHex = mode.hexCode
                if (mode.foreground) {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateTextColor(Color.valueOf(colorHex))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateTextColor(Color.valueOf(colorHex))
                } else {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                }
            }
        }
    }

    private class EscapeColor256Action {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeColor256Mode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                val foreground = params!![0].toString().toBoolean()
                val mode = escapeMode as EscapeColor256Mode
                val colorHex = mode.hexCode
                if (foreground) {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateTextColor(Color.valueOf(colorHex))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateTextColor(Color.valueOf(colorHex))
                } else {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.valueOf(colorHex))
                }
            }
        }
    }

    private class EscapeColorRgbAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeColorRgbMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                val foreground = params!![0].toString().toBoolean()
                val r = params[2].toString().toDouble()
                val g = params[3].toString().toDouble()
                val b = params[4].toString().toDouble()
                if (foreground) {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateTextColor(Color.color(r, g, b))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateTextColor(Color.color(r, g, b))
                } else {
                    executeTarget.currentChineseTextStyle =
                        executeTarget.currentChineseTextStyle.updateBackgroundColor(Color.color(r, g, b))
                    executeTarget.currentEnglishTextStyle =
                        executeTarget.currentEnglishTextStyle.updateBackgroundColor(Color.color(r, g, b))
                }
            }
        }
    }


    private class EscapeOscBelAction {
        companion object Instance : AnsiEscapeAction<EscapedStyleClassedArea>() {
            override fun focusMode(): Class<out IEscapeMode> {
                return EscapeOSCMode::class.java
            }

            override fun action(executeTarget: EscapedStyleClassedArea, escapeMode: IEscapeMode, params: List<Any>?) {
                val parameter = params!![0]
                when (escapeMode) {
                    RENAMING_TAB_TITLE_0 -> println("Renaming tab: $parameter")
                    RENAMING_TAB_TITLE_1 -> println("Renaming tab: $parameter")
                    RENAMING_WIDOW_TITLE -> println("Renaming window: $parameter")
                    ECHO_WORKING_DOCUMENT -> {}
                    ECHO_WORKING_DIRECTORY -> {}
                }
            }
        }
    }

    init {
        this.registerComponent(id)

        cursor = Cursor(id)

        textProperty().addListener { _: ObservableValue<out String>?, oldVal: String, newVal: String ->
            cursor.update(newVal.length - oldVal.length)
            cursor.inlinePosition = if (cursor.inlinePosition > length) length else cursor.inlinePosition
            if (paragraphSizeWatch != paragraphs.size && newVal.replace(oldVal, "") != StrUtil.LF) {
                updateLineIndexParagraphIndexMap(paragraphs.size - 1)
                paragraphSizeWatch = paragraphs.size
                System.gc()
            }
        }

        widthProperty().addListener { _, oldVal, newVal ->
            if (newVal != oldVal) {
                sizeChanged = true
            }
        }
    }

    override fun initialize() {
        StageHolder.scene?.addEventHandler(MouseEvent.MOUSE_RELEASED) { event ->
            if (event.button == MouseButton.PRIMARY && sizeChanged) {
                sizeChanged = false
                updateLineIndexParagraphIndexMap(null)
            }
        }
    }

    private fun updateLineIndexParagraphIndexMap(currentParagraph: Int?) {
        if (currentParagraph == null) {
            lineIndex = 1
            lastParagraph = 0
            lineIndexParagraphIndexMap.clear()
            paragraphs.forEachIndexed { index, _ ->
                for (i in 1..getParagraphLinesCount(index)) {
                    lineIndexParagraphIndexMap[lineIndex++] = index
                }
                lastParagraph = index
            }
            // in XTerm console, line 0 equals line 1
            lineIndexParagraphIndexMap[0] = 0
        } else if (currentParagraph != 0) {
            for (i in lastParagraph..currentParagraph) {
                for (j in 1..getParagraphLinesCount(i)) {
                    lineIndexParagraphIndexMap[lineIndex++] = i
                }
            }
            lastParagraph = currentParagraph + 1
            lineIndexParagraphIndexMap[0] = 0
        } else {
            lineIndex = 1
            lastParagraph = 0
            lineIndexParagraphIndexMap.clear()
            lineIndexParagraphIndexMap[0] = 0
        }
    }

    companion object {
        private var actionMap: Map<Class<out IEscapeMode>, AnsiEscapeAction<EscapedStyleClassedArea>>? = null

        init {
            val actions: MutableList<AnsiEscapeAction<EscapedStyleClassedArea>> = ArrayList()
            actions.add(EscapeAsciiControlAction.Instance)
            actions.add(EscapeCursorControlAction.Instance)
            actions.add(EscapeEraseFunctionsAction.Instance)
            actions.add(EscapeColorGraphicsCombineAction.Instance)
            actions.add(EscapeColorGraphicsAction.Instance)
            actions.add(EscapeColor8To16Action.Instance)
            actions.add(EscapeColorISOAction.Instance)
            actions.add(EscapeColor256Action.Instance)
            actions.add(EscapeColorRgbAction.Instance)
            actions.add(EscapeOscBelAction.Instance)
            val map: MutableMap<Class<out IEscapeMode>, AnsiEscapeAction<EscapedStyleClassedArea>> = HashMap()
            for (action in actions) {
                map[action.focusMode()] = action
            }
            actionMap = ImmutableMap.copyOf(map)
        }
    }
}