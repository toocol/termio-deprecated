package com.toocol.termio.utilities.escape

import com.google.common.collect.ImmutableMap
import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction
import com.toocol.termio.utilities.log.Loggable
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.StrUtil
import com.toocol.termio.utilities.utils.Tuple2
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/8 10:45
 */
class AnsiEscapeSearchEngine<T : EscapeCodeSequenceSupporter<T>?>(private val executeTarget: T) : Loggable, Castable {
    private var actionMap: Map<Class<out IEscapeMode>, AnsiEscapeAction<T>>? = null

    companion object {
        private val wordNumberRegex = Regex(pattern = """\w+""")
        private val numberRegex = Regex(pattern = """\d+""")
        private val wordRegex = Regex(pattern = """[a-zA-Z]+""")
        private val codeStringRegex = Regex(pattern = """(\d{1,3};){1,2}[\\"'\w ]+;?""")
        private val codeRegex = Regex(pattern = """(\d{1,3};)+""")
        private val stringRegex = Regex(pattern = """[\w ]+;?""")

        private const val uberEscapeModeRegexPattern = """(\u001b\[\d{1,4};\d{1,4}[Hf])""" +
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
        private val screenModePatter = Regex(pattern = """\u001b\[=\d{1,2}h""")
        private val disableScreenModeRegex = Regex(pattern = """\u001b\[=\d{1,2}l""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#common-private-modes
        private val commonPrivateModeRegex = Regex(pattern = """\u001b\[\?\d{2,4}[lh]""")

        // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#keyboard-strings
        private val keyBoardStringModeRegex =
            Regex(pattern = """\u001b\[((\d{1,3};){1,2}(((\\")|'|")[\\w ]+((\\")|'|");?)|(\d{1,2};?))+p""")
    }

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
    fun actionOnEscapeMode(text: String) {
        if (actionMap == null) {
            warn("AnsiEscapeSearchEngine is not initialized.")
            return
        }
        val queue: Queue<Tuple2<IEscapeMode?, List<Any>>> = ArrayDeque()
        val split = text.split(uberEscapeModeRegex).toTypedArray()
        uberEscapeModeRegex.findAll(text).forEach { lineRet ->
            val escapeSequence = lineRet.value;
            val params: List<Any> = ArrayList()
            val escapeMode: IEscapeMode? = null
            val dealAlready = AtomicBoolean()

            regexParse(escapeSequence, cursorSetPosModeRegex, { regex: Regex ->
                val escapeModeStr = regex.find(escapeSequence, 0)
            }, dealAlready)

            regexParse(escapeSequence, cursorControlModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, eraseFunctionModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, colorGraphicsModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, color256ModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, colorRgbModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, screenModePatter, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, disableScreenModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, commonPrivateModeRegex, { regex: Regex ->

            }, dealAlready)

            regexParse(escapeSequence, keyBoardStringModeRegex, { regex: Regex ->

            }, dealAlready)

            queue.offer(Tuple2(escapeMode, params))
        }
        for (sp in split) {
            if (StrUtil.isNotEmpty(sp)) {
                executeTarget!!.printOut(sp)
            }
            if (!queue.isEmpty()) {
                val tuple = queue.poll()
                actionMap!![tuple._1()!!.javaClass]!!.action(executeTarget, tuple._1(), tuple._2())
            }
        }
    }

    private fun regexParse(text: String, regex: Regex, consumer: Consumer<Regex>, dealAlready: AtomicBoolean) {
        if (dealAlready.get()) {
            return
        }
        if (!regex.containsMatchIn(text)) {
            return;
        }
        consumer.accept(regex)
        dealAlready.set(true)
    }

    private fun registerActions(actions: List<AnsiEscapeAction<T>>) {
        val map: MutableMap<Class<out IEscapeMode>, AnsiEscapeAction<T>> = HashMap()
        for (action in actions) {
            map[action.focusMode()] = action
        }
        actionMap = ImmutableMap.copyOf(map)
    }

    init {
        registerActions(executeTarget!!.registerActions())
    }
}