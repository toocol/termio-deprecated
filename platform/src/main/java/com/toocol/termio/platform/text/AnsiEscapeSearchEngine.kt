package com.toocol.termio.platform.text

import com.toocol.termio.utilities.escape.*
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import javafx.application.Platform
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

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

        private const val uberEscapeModeRegexPattern = """\u0007|\u0008|\u000d|(\u001b\[\d{1,4};\d{1,4}[Hf])""" +
                """|((\u001b\[\d{0,4}([HABCDEFGsu]|(6n)))|(\u001b [M78]))""" +
                """|(\u001b\[[0123]?[JK])""" +
                """|(\u001b\[((?!38)(?!48)\d{1,3};?)+m)""" +
                """|(\u001b\[(38)?(48)?;5;\d{1,3}m)""" +
                """|(\u001b\[(38)?(48)?;2;\d{1,3};\d{1,3};\d{1,3}m)""" +
                """|(\u001b\[=\d{1,2}h)""" +
                """|(\u001b\[=\d{1,2}l)""" +
                """|(\u001b\[\?\d{2,4}[lh])""" +
                """|(\u001b\[((\d{1,3};){1,2}(((\\")|'|")[\w ]+((\\")|'|");?)|(\d{1,2};?))+p)""" +
                """|\u001b]\d;.+\u0007"""
        private val uberEscapeModeRegex = Regex(pattern = uberEscapeModeRegexPattern)

        private val belModeRegex = Regex(pattern = """\u0007""")
        private val backspaceModeRex = Regex(pattern = """\u0008""")
        private val enterModeRegex = Regex(pattern = """\u000d""")

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
        private val keyBoardStringModeRegex =
            Regex(pattern = """\u001b\[((\d{1,3};){1,2}(((\\")|'|")[\w ]+((\\")|'|");?)|(\d{1,2};?))+p""")

        // see: https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h2-Definitions
        private val oscBelRegex = Regex(pattern = """\u001b][01267];.+\u0007""")
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
        uberEscapeModeRegex.findAll(text).forEach { lineRet ->
            val escapeSequence = lineRet.value
            val dealAlready = AtomicBoolean()
            val tuple = tuple()

            parseRegex(escapeSequence, belModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                tuple.first(EscapeAsciiControlMode.BEL)
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, backspaceModeRex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                tuple.first(EscapeAsciiControlMode.BACKSPACE)
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, enterModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                tuple.first(EscapeAsciiControlMode.ENTER)
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, cursorSetPosModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                tuple.first(EscapeCursorControlMode.codeOf("Hf")).second(
                    numberRegex.findAll(it)
                        .asSequence()
                        .map { matchResult -> matchResult.value.toInt() }
                        .toList()
                )
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, cursorControlModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                val code = wordNumberRegex.find(it)?.value ?: return@let null
                if (code.contains("A") || code.contains("B") || code.contains("C") || code.contains("D")
                    || code.contains("E") || code.contains("F") || code.contains("G")
                ) {
                    val icode = wordRegex.find(code)?.value
                    var num = numberRegex.find(code)?.value
                    if (num == null) num = "1"
                    tuple.first(EscapeCursorControlMode.codeOf(icode)).second(intArrayOf(num.toInt()).toList())
                } else {
                    tuple.first(EscapeCursorControlMode.codeOf(code))
                }
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, eraseFunctionModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                val code = wordNumberRegex.find(it)?.value
                code ?: return@let null
                tuple.first(EscapeEraseFunctionsMode.codeOf(code))
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, colorGraphicsModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                numberRegex.findAll(it)
                    .asSequence()
                    .forEach { result ->
                        run {
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
                        }
                    }
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, color256ModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                var params: List<Any> = listOf()
                var mode: IEscapeMode? = null
                numberRegex.findAll(it)
                    .asSequence()
                    .forEachIndexed { index, matchResult ->
                        run {
                            when (index) {
                                // 38-foreground 48-background
                                0 -> params = params.plusElement(matchResult.value == "38")
                                1 -> if (matchResult.value != "5") return@let null
                                2 -> {
                                    val code = matchResult.value.toInt()
                                    mode = EscapeColor256Mode.codeOf(code)
                                }
                                else -> {}
                            }
                        }
                    }
                tuple.first(mode).second(params)
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, colorRgbModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                var params: List<Any> = listOf()
                numberRegex.findAll(it)
                    .asSequence()
                    .forEachIndexed { index, matchResult ->
                        run {
                            when (index) {
                                // 38-foreground 48-background
                                0 -> params = params.plusElement(matchResult.value == "38")
                                1 -> if (matchResult.value != "2") return@let null
                                // R
                                2 -> params = params.plusElement(matchResult.value.toInt())
                                // G
                                3 -> params = params.plusElement(matchResult.value.toInt())
                                // B
                                4 -> params = params.plusElement(matchResult.value.toInt())
                                else -> {}
                            }
                        }
                    }
                tuple.first(EscapeColorRgbMode.COLOR_RGB_MODE).second(params)
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, screenModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                val code = numberRegex.find(it)?.value?.toInt()
                code ?: return@let null
                // enable screen mode
                tuple.first(EscapeScreenMode.codeOf(code)).second(listOf(true))
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, disableScreenModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                val code = numberRegex.find(it)?.value?.toInt()
                code ?: return@let null
                // enable screen mode
                tuple.first(EscapeScreenMode.codeOf(code)).second(listOf(false))
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, commonPrivateModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                val code = wordNumberRegex.find(it)?.value
                code ?: return@let null
                tuple.first(EscapeCommonPrivateMode.codeOf(code))
                tuple._1() ?: return@let null
                tuple
            }?.apply { queue.offer(this) }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, keyBoardStringModeRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                codeStringRegex.findAll(it)
                    .asSequence()
                    .forEach { matchResult ->
                        run {
                            val codeString = matchResult.value
                            var code = codeRegex.find(codeString)?.value
                            code ?: return@run
                            var string = stringRegex.find(codeString.replace(code, StrUtil.EMPTY))?.value
                            string ?: return@run

                            code = if (code[code.length - 1] == ';') code.substring(0, code.length - 1) else code
                            string = if (string[string.length - 1] == ';') string.substring(0,
                                string.length - 1) else string
                            val tp: Tuple2<IEscapeMode, List<Any>> = Tuple2()
                            tp.first(EscapeKeyBoardStringMode.KEY_BOARD_STRING_MODE).second(listOf(code, string))
                            queue.offer(tp)
                        }
                    }
            }

            if (dealAlready.get()) return@forEach
            parseRegex(escapeSequence, oscBelRegex, dealAlready)?.takeIf { it.isNotEmpty() }?.let {
                val split = it.replace("\u001b]", "").replace("\u0007", "").split(";")
                if (split.size != 2) return@let null
                val code = split[0].toInt()
                tuple.first(EscapeOSCMode.codeOf(code))
                tuple._1() ?: return@let null
                val parameter = split[1]
                tuple.second(listOf(parameter))
                tuple
            }?.apply { queue.offer(this) }
        }

        val actionMap = executeTarget.getActionMap()
        val split = text.split(uberEscapeModeRegex).toTypedArray()
        val latch = CountDownLatch(1)
        Platform.runLater {
            val startTime = System.currentTimeMillis()
            split.forEach { sp ->
                if (StrUtil.isNotEmpty(sp)) {
                    executeTarget.printOut(sp)
                }
                if (!queue.isEmpty()) {
                    val tuple = queue.poll()
                    val ansiEscapeAction = actionMap!![tuple._1()!!.javaClass]
                    ansiEscapeAction ?: return@forEach
                    ansiEscapeAction.action(executeTarget, tuple._1(), tuple._2())
                }
            }
            println("Spend time: ${(System.currentTimeMillis() - startTime)}ms")
            queue.clear()
            latch.countDown()
            System.gc()
        }
        latch.await()
    }

    private fun tuple(): Tuple2<IEscapeMode, List<Any>> {
        return Tuple2()
    }

    private fun parseRegex(text: String, regex: Regex, dealAlready: AtomicBoolean): String? {
        if (dealAlready.get()) return null
        val match = regex.find(text) ?: return null

        dealAlready.set(true)
        return match.value
    }
}