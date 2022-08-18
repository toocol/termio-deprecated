package com.toocol.termio.utilities.ansi

import com.toocol.termio.utilities.ansi.ColorHelper.background
import com.toocol.termio.utilities.ansi.ColorHelper.front
import com.toocol.termio.utilities.ansi.CursorPositionHelper.cursorMove
import com.toocol.termio.utilities.utils.ASCIIStrCache
import com.toocol.termio.utilities.utils.StrUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/26 19:14
 */
class AnsiStringBuilder {
    private val builder = StringBuilder()

    private var colorMode = ColorMode.COLOR_256
    private var bg256 = -1
    private var ft256 = -1
    private var bgR = -1
    private var bgG = -1
    private var bgB = -1
    private var ftR = -1
    private var ftG = -1
    private var ftB = -1

    fun front(color: Int): AnsiStringBuilder {
        if (color < 0 || color > 255) {
            return this
        }
        colorMode = ColorMode.COLOR_256
        ft256 = color
        return this
    }

    fun background(color: Int): AnsiStringBuilder {
        if (color < 0 || color > 255) {
            return this
        }
        colorMode = ColorMode.COLOR_256
        bg256 = color
        return this
    }

    fun front(r: Int, g: Int, b: Int): AnsiStringBuilder {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            return this
        }
        colorMode = ColorMode.COLOR_RGB
        ftR = r
        ftG = g
        ftB = b
        return this
    }

    fun background(r: Int, g: Int, b: Int): AnsiStringBuilder {
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            return this
        }
        colorMode = ColorMode.COLOR_RGB
        bgR = r
        bgG = g
        bgB = b
        return this
    }

    fun deFront(): AnsiStringBuilder {
        ft256 = -1
        ftR = -1
        ftG = -1
        ftB = -1
        return this
    }

    fun deBackground(): AnsiStringBuilder {
        bg256 = -1
        bgR = -1
        bgG = -1
        bgB = -1
        return this
    }

    fun append(str: String): AnsiStringBuilder {
        if (StringUtils.isEmpty(str)) {
            return this
        }
        builder.append(fillColor(str))
        return this
    }

    fun append(strConst: String, line: Int, column: Int): AnsiStringBuilder {
        var str = strConst
        if (StringUtils.isEmpty(str)) {
            return this
        }
        str = cursorMove(fillColor(str), line, column)
        builder.append(str)
        return this
    }

    fun append(ch: Char): AnsiStringBuilder {
        val str = ASCIIStrCache.toString(ch)
        return append(str)
    }

    fun append(integer: Int): AnsiStringBuilder {
        val str = integer.toString()
        return append(str)
    }

    fun append(l: Long): AnsiStringBuilder {
        val str = l.toString()
        return append(str)
    }

    fun append(sb: StringBuilder): AnsiStringBuilder {
        if (sb.isEmpty()) {
            return this
        }
        val str = sb.toString()
        return append(str)
    }

    fun append(ansiSb: AnsiStringBuilder): AnsiStringBuilder {
        builder.append(ansiSb.toString())
        return this
    }

    fun crlf(): AnsiStringBuilder {
        builder.append(StrUtil.CRLF)
        return this
    }

    fun tab(): AnsiStringBuilder {
        return append(StrUtil.TAB)
    }

    fun space(): AnsiStringBuilder {
        return append(StrUtil.SPACE)
    }

    fun space(cnt: Int): AnsiStringBuilder {
        return append(StrUtil.SPACE.repeat(cnt))
    }

    fun clearStr(): AnsiStringBuilder {
        builder.delete(0, builder.length)
        return this
    }

    fun clearColor(): AnsiStringBuilder {
        ft256 = -1
        bg256 = -1
        ftR = -1
        ftG = -1
        ftB = -1
        bgR = -1
        bgG = -1
        bgB = -1
        return this
    }

    private fun fillColor(strConst: String): String {
        var str = strConst
        if (colorMode == ColorMode.COLOR_256) {
            if (ft256 != -1) {
                str = front(str, ft256)
            }
            if (bg256 != -1) {
                str = background(str, bg256)
            }
        } else if (colorMode == ColorMode.COLOR_RGB) {
            if (ftR != -1 && ftG != -1 && ftB != -1) {
                str = front(str, ftR, ftG, ftB)
            }
            if (bgR != -1 && bgG != -1 && bgB != -1) {
                str = background(str, bgR, bgG, bgB)
            }
        }
        return str
    }

    override fun toString(): String {
        return builder.toString()
    }

    fun length(): Int {
        return builder.length
    }

    enum class ColorMode {
        COLOR_256, COLOR_RGB
    }
}