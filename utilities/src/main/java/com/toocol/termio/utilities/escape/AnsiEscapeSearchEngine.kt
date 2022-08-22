package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/8 10:45
 */
class AnsiEscapeSearchEngine<T : EscapeCodeSequenceSupporter<T>> : Loggable, Castable {
    companion object {
        private val wordNumberRegex = Regex(pattern = """\w+""")
        private val numberRegex = Regex(pattern = """\d+""")
        private val wordRegex = Regex(pattern = """[a-zA-Z]+""")
        private val codeStringRegex = Regex(pattern = """(\d{1,3};){1,2}[\\"'\w ]+;?""")
        private val codeRegex = Regex(pattern = """(\d{1,3};)+""")
        private val stringRegex = Regex(pattern = """[\w ]+;?""")

        private const val uberEscapeModeRegexPattern = """\r|(\u001b\[\d{1,4};\d{1,4}[Hf])""" +
                """|((\u001b\[\d{0,4}([HABCDEFGsu]|(6n)))|(\u001b [M78]))""" +
                """|(\u001b\[[0123]?[JK])""" +
                """|(\u001b\[((?!38)(?!48)\d{1,3};?)+m)""" +
                """|(\u001b\[(38)?(48)?;5;\d{1,3}m)""" +
                """|(\u001b\[(38)?(48)?;2;\d{1,3};\d{1,3};\d{1,3}m)""" +
                """|(\u001b\[=\d{1,2}h)""" +
                """|(\u001b\[=\d{1,2}l)""" +
                """|(\u001b\[\?\d{2,4}[lh])""" +
                """|(\u001b\[((\d{1,3};){1,2}(((\\")|'|")[\w ]+((\\")|'|");?)|(\d{1,2};?))+p)"""
        private val uberEscapeModeRegex = Regex(pattern = uberEscapeModeRegexPattern)

        private val enterModeRegex = Regex(pattern = "\r")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#cursor-controls
        private val cursorSetPosModeRegex = Regex(pattern = """\u001b\[\d{1,4};\d{1,4}[Hf]""")
        private val cursorControlModeRegex = Regex(pattern = """(\u001b\[\d{0,4}([HABCDEFGsu]|(6n)))|(\u001b [M78])""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#erase-functions
        private val eraseFunctionModeRegex = Regex(pattern = """\u001b\[[0123]?[JK]""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#colors--graphics-mode
        private val colorGraphicsModeRegex = Regex(pattern = """\u001b\[((?!38)(?!48)\d{1,3};?)+m""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#256-colors
        private val color256ModeRegex = Regex(pattern = """\u001b\[(38)?(48)?;5;\d{1,3}m""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#rgb-colors
        private val colorRgbModeRegex = Regex(pattern = """\u001b\[(38)?(48)?;2;\d{1,3};\d{1,3};\d{1,3}m""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#set-mode
        private val screenModeRegex = Regex(pattern = """\u001b\[=\d{1,2}h""")
        private val disableScreenModeRegex = Regex(pattern = """\u001b\[=\d{1,2}l""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#common-private-modes
        private val commonPrivateModeRegex = Regex(pattern = """\u001b\[\?\d{2,4}[lh]""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#keyboard-strings
        private val keyBoardStringModeRegex = Regex(pattern = """\u001b\[((\d{1,3};){1,2}(((\\")|'|")[\w ]+((\\")|'|");?)|(\d{1,2};?))+p""")
    }

    private val queue: Queue<Tuple2<IEscapeMode, List<Any>>> = ArrayDeque()

    /*
     * Suppose we have such text:
     * ######ESC[K######
     * |
     * | split text by escape sequence, generate escape sequence queue.
     * ↓
     * ["######", "######"]
     * Queue<Tuple<IEscapeMode,List<Object>>>
     *
     * Then we print out the split text in loop, and getting and invoking AnsiEscapeAction from the head of Queue<Tuple<IEscapeMode,List<Object>>>.
     */
    @Synchronized
    fun actionOnEscapeMode(text: String, executeTarget: T) {
        val split = text.split(uberEscapeModeRegex).toTypedArray()
        uberEscapeModeRegex.findAll(text).forEach { lineRet ->
            val escapeSequence = lineRet.value
            val dealAlready = AtomicBoolean()

            regexParse(escapeSequence, enterModeRegex, { _: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                tuple.first(EscapeEnterMode.ENTER)
            }, dealAlready)

            regexParse(escapeSequence, cursorSetPosModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                tuple.first(EscapeCursorControlMode.codeOf("Hf")).second(
                    numberRegex.findAll(certainEscape)
                        .asSequence()
                        .map { matchResult -> matchResult.value.toInt() }
                        .toList()
                )
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, cursorControlModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                val code = wordNumberRegex.find(certainEscape)?.value ?: return@regexParse
                if (code.contains("A") || code.contains("B") || code.contains("C") || code.contains("D")
                    || code.contains("E") || code.contains("F") || code.contains("G")) {
                    val icode = wordRegex.find(code)?.value
                    var num = numberRegex.find(code)?.value
                    if (num == null) num = "1"
                    tuple.first(EscapeCursorControlMode.codeOf(icode)).second(intArrayOf(num.toInt()).toList())
                } else {
                    tuple.first(EscapeCursorControlMode.codeOf(code))
                }
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, eraseFunctionModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                val code = wordNumberRegex.find(certainEscape)?.value
                code ?: return@regexParse
                tuple.first(EscapeEraseFunctionsMode.codeOf(code))
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, colorGraphicsModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                numberRegex.findAll(certainEscape)
                    .asSequence()
                    .forEach { result -> run {
                        val code = result.value.toInt()
                        val mode = if (code < 30) {
                            EscapeColorGraphicsMode.codeOf(code)
                        } else if (code in 30..49) {
                            EscapeColor8To16Mode.codeOf(code)
                        } else if (code in 90..107) {
                            EscapeColorISOMode.codeOf(code)
                        } else {
                            null
                        }
                        tuple.first(mode)
                    } }
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, color256ModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                var params: List<Any> = listOf()
                var mode: IEscapeMode? = null
                var illegalClz = false
                numberRegex.findAll(certainEscape)
                    .asSequence()
                    .forEachIndexed { index, matchResult -> run {
                        when (index) {
                            // 38-foreground 48-background
                            0 -> params = params.plusElement(matchResult.value == "38")
                            1 -> {
                                illegalClz = matchResult.value != "5"
                                if (illegalClz) return@forEachIndexed
                            }
                            2 -> {
                                val code = matchResult.value.toInt()
                                mode = EscapeColor256Mode.codeOf(code)
                            }
                            else -> {}
                        }
                    } }
                if (illegalClz) return@regexParse
                tuple.first(mode).second(params)
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, colorRgbModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                var params: List<Any> = listOf()
                var illegalClz = false
                numberRegex.findAll(certainEscape)
                    .asSequence()
                    .forEachIndexed { index, matchResult -> run {
                        when (index) {
                            // 38-foreground 48-background
                            0 -> params = params.plusElement(matchResult.value == "38")
                            1 -> {
                                illegalClz = matchResult.value != "2"
                                if (illegalClz) return@forEachIndexed
                            }
                            // R
                            2 -> params = params.plusElement(matchResult.value.toInt())
                            // G
                            3 -> params = params.plusElement(matchResult.value.toInt())
                            // B
                            4 -> params = params.plusElement(matchResult.value.toInt())
                            else -> {}
                        }
                    }}
                if (illegalClz) return@regexParse
                tuple.first(EscapeColorRgbMode.COLOR_RGB_MODE).second(params)
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, screenModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                val code = numberRegex.find(certainEscape)?.value?.toInt()
                code ?: return@regexParse
                // enable screen mode
                tuple.first(EscapeScreenMode.codeOf(code)).second(listOf(true))
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, disableScreenModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                val code = numberRegex.find(certainEscape)?.value?.toInt()
                code ?: return@regexParse
                // disable screen mode
                tuple.first(EscapeScreenMode.codeOf(code)).second(listOf(false))
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, commonPrivateModeRegex, { certainEscape: String, tuple: Tuple2<IEscapeMode, List<Any>> ->
                val code = wordNumberRegex.find(certainEscape)?.value
                code ?: return@regexParse
                tuple.first(EscapeCommonPrivateMode.codeOf(code))
            }, dealAlready)?.apply { queue.offer(this) }

            regexParse(escapeSequence, keyBoardStringModeRegex, { certainEscape: String, _: Tuple2<IEscapeMode, List<Any>> ->
                codeStringRegex.findAll(certainEscape)
                    .asSequence()
                    .forEach { matchResult -> run {
                        val codeString = matchResult.value
                        var code = codeRegex.find(codeString)?.value
                        code ?: return@run
                        var string = stringRegex.find(codeString.replace(code, StrUtil.EMPTY))?.value
                        string ?: return@run

                        code = if (code[code.length - 1] == ';') code.substring(0, code.length - 1) else code
                        string = if (string[string.length - 1] == ';') string.substring(0, string.length - 1) else string
                        val tp: Tuple2<IEscapeMode, List<Any>> = Tuple2()
                        tp.first(EscapeKeyBoardStringMode.KEY_BOARD_STRING_MODE).second(listOf(code, string))
                        queue.offer(tp)
                    } }
            }, dealAlready)
        }
        val actionMap = executeTarget.getActionMap()
        for (sp in split) {
            if (StrUtil.isNotEmpty(sp)) {
                executeTarget.printOut(sp)
            }
            if (!queue.isEmpty()) {
                val tuple = queue.poll()
                val ansiEscapeAction = actionMap!![tuple._1()!!.javaClass]
                ansiEscapeAction ?: continue
                ansiEscapeAction.action(executeTarget, tuple._1(), tuple._2())
            }
        }
        queue.clear()
    }

    private fun regexParse(
        text: String,
        regex: Regex,
        func: BiConsumer<String, Tuple2<IEscapeMode, List<Any>>>,
        dealAlready: AtomicBoolean,
    )
    : Tuple2<IEscapeMode, List<Any>>? {

        if (dealAlready.get()) return null
        val match = regex.find(text) ?: return null

        val tuple : Tuple2<IEscapeMode, List<Any>> = Tuple2()
        dealAlready.set(true)
        func.accept(match.value, tuple)
        tuple._1() ?: return null
        return tuple
    }
}