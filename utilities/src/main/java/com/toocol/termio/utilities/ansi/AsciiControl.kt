package com.toocol.termio.utilities.ansi

import com.toocol.termio.utilities.utils.StrUtil

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/2 19:00
 * @version: 0.0.1
 */
object AsciiControl {
    const val SOH = "\u0001"
    const val STX = "\u0002"
    const val ETX = "\u0003"
    const val EOT = "\u0004"
    const val ENQ = "\u0005"
    const val ACK = "\u0006"
    const val BEL = "\u0007"
    const val BS = "\b"
    const val HT = "\u0009"
    const val LF = "\n"
    const val VT = "\u000B"
    const val FF = "\u000C"
    const val CR = "\r"
    const val SO = "\u000E"
    const val SI = "\u000F"
    const val DLE = "\u0010"
    const val DC1 = "\u0011"
    const val DC2 = "\u0012"
    const val DC3 = "\u0013"
    const val DC4 = "\u0014"
    const val NAK = "\u0015"
    const val SYN = "\u0016"
    const val ETB = "\u0017"
    const val CAN = "\u0018"
    const val EM = "\u0019"
    const val SUB = "\u001A"
    const val ESCAPE = "\u001B"
    const val FS = "\u001C"
    const val GS = "\u001D"
    const val RS = "\u001E"
    const val US = "\u001F"
    const val DEL = "\u007F"
    const val UNKNOWN = "�"

    // this two ANSI Escape Sequences was useless under Windows console.
    private val IGNORES = arrayOf(arrayOf("\u001B[?25h", """\u001B\[\?25h"""), arrayOf("\u001B[?25l", """\u001B\[\?25l"""))
    private val CLEAN_PATTERNS = arrayOf(
        """\u001b\[#?=?\??[0-9]*[a-zA-Z]""",
        """\u001b\[[0-9]+;[0-9]+[a-zA-Z]""",
        """\u001b\[[0-9]+;[0-9]+;.+m"""
    )

    const val ANIS_ESCAPE_CURSOR_LOCATION = """\u001b\[[0-9]+;{}H"""
    const val ANIS_CLEAR_ALL_MODE = "\u001b[0m"
    const val ANIS_ERASE_LINE = "\u001b[K"
    const val ANIS_ERASE_SCREEN = "\u001b[2J"

    private const val ANIS_ESCAPE_POSITION = """\u001b\[#?\??[0-9]*;?[0-9]*[fjklhrABCDEFGJH]"""
    private const val ANIS_ESCAPE_MOSH_ROLLING = """\u001b\[0m\u001b\[[0-9]*;[0-9]*r\u001b\[[0-9]*;[0-9]*H"""
    private const val ANIS_ESCAPE_DOUBLE_CURSOR_LOCATION = """\u001b\[[0-9]+;[0-9]+H[a-zA-Z0-9_~/]+\u001b\[[0-9]+;[0-9]+H"""
    private const val ANIS_ESCAPE_CURSOR_BRACKET_K = """\u001b\[[0-9]+;[0-9]+H[a-zA-Z0-9_~/\]\[# ]+\u001b\[K"""
    private const val ANIS_CURSOR_POSITION = """[0-9]+;[0-9]+"""

    @JvmField
    val ANIS_ESCAPE_MOSH_ROLLING_PATTERN = Regex(ANIS_ESCAPE_MOSH_ROLLING)
    @JvmField
    val ANIS_ESCAPE_DOUBLE_CURSOR_PATTERN = Regex(ANIS_ESCAPE_DOUBLE_CURSOR_LOCATION)
    @JvmField
    val ANIS_ESCAPE_CURSOR_BRACKET_K_PATTERN = Regex(ANIS_ESCAPE_CURSOR_BRACKET_K)
    @JvmField
    val ANIS_CURSOR_POSITION_PATTERN = Regex(ANIS_CURSOR_POSITION)

    @JvmStatic
    fun ignore(sourceConst: String): String {
        var source = sourceConst
        for (replace in IGNORES) {
            if (source.contains(replace[0])) source = source.replace(replace[1].toRegex(), StrUtil.EMPTY)
        }
        return source
    }

    @JvmStatic
    fun detectRolling(msg: String): Boolean {
        val matcher = ANIS_ESCAPE_MOSH_ROLLING_PATTERN.find(msg)
        return matcher != null
    }

    @JvmStatic
    fun clean(strConst: String): String {
        var str = strConst
        for (cleanPattern in CLEAN_PATTERNS) {
            str = str.replace(cleanPattern.toRegex(), StrUtil.EMPTY)
        }
        return str
    }

    fun cleanPositionAnisEscape(str: String): String {
        return str.replace(ANIS_ESCAPE_POSITION.toRegex(), StrUtil.EMPTY)
    }

    @JvmStatic
    fun setCursorToLineHead(line: Int): String {
        return "$ESCAPE[$line;0H"
    }

    @JvmStatic
    fun extractCursorPosition(str: String): IntArray {
        val matcher = ANIS_CURSOR_POSITION_PATTERN.find(str)
        if (matcher != null) {
            val split = matcher.value.split(";").toTypedArray()
            return intArrayOf(split[0].toInt(), split[1].toInt())
        }
        return intArrayOf(0, 0)
    }

    @JvmStatic
    fun cleanCursorMode(str: String): String {
        return str.replace("""\u001b\[[0-9]+;[0-9]+H""".toRegex(), "").replace("""\u001b\[K""".toRegex(), "")
    }
}