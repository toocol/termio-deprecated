package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.ansi.ColorClut

enum class EscapeColor8To16Mode(val colorCode: Int, val hexCode: String, val foreground: Boolean) : IEscapeMode {
    FOREGROUND_BLACK(30, ColorClut.Black.hex, true),
    FOREGROUND_RED(31, ColorClut.Red.hex, true),
    FOREGROUND_GREEN(32, ColorClut.Green.hex, true),
    FOREGROUND_YELLOW(33, ColorClut.Yellow.hex, true),
    FOREGROUND_BLUE(34, ColorClut.Blue.hex, true),
    FOREGROUND_MAGENTA(35, ColorClut.Magenta.hex, true),
    FOREGROUND_CYAN(36, ColorClut.Cyan.hex, true),
    FOREGROUND_WHITE(37, ColorClut.White.hex, true),
    FOREGROUND_DEFAULT(39, ColorClut.White.hex, true),
    BACKGROUND_BLACK(40, ColorClut.Black.hex, false),
    BACKGROUND_RED(41, ColorClut.Red.hex, false),
    BACKGROUND_GREEN(42, ColorClut.Green.hex, false),
    BACKGROUND_YELLOW(43, ColorClut.Yellow.hex, false),
    BACKGROUND_BLUE(44, ColorClut.Blue.hex, false),
    BACKGROUND_MAGENTA(45, ColorClut.Magenta.hex, false),
    BACKGROUND_CYAN(46, ColorClut.Cyan.hex, false),
    BACKGROUND_WHITE(47, ColorClut.White.hex, false),
    BACKGROUND_DEFAULT(49, ColorClut.Black.hex, false);

    companion object {
        private val colorHexMap: MutableMap<Int, String> = HashMap()

        init {
            for (color in values()) {
                colorHexMap[color.colorCode] = color.hexCode
            }
        }

        fun codeOf(code: Int): EscapeColor8To16Mode? {
            for (value in values()) {
                if (value.colorCode == code) {
                    return value
                }
            }
            return null
        }

        fun hexOf(colorCode: Int): String? {
            return colorHexMap[colorCode]
        }
    }
}