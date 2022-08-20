package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.ansi.ColorClut

enum class EscapeColor8To16Mode(val colorCode: Int, val hexCode: String) : IEscapeMode {
    FOREGROUND_BLACK(30, ColorClut.Black.hex),
    FOREGROUND_RED(31, ColorClut.Red.hex),
    FOREGROUND_GREEN(32, ColorClut.Green.hex),
    FOREGROUND_YELLOW(33, ColorClut.Yellow.hex),
    FOREGROUND_BLUE(34, ColorClut.Blue.hex),
    FOREGROUND_MAGENTA(35, ColorClut.Magenta.hex),
    FOREGROUND_CYAN(36, ColorClut.Cyan.hex),
    FOREGROUND_WHITE(37, ColorClut.White.hex),
    FOREGROUND_DEFAULT(39, ColorClut.White.hex),
    BACKGROUND_BLACK(40, ColorClut.Black.hex),
    BACKGROUND_RED(41, ColorClut.Red.hex),
    BACKGROUND_GREEN(42, ColorClut.Green.hex),
    BACKGROUND_YELLOW(43, ColorClut.Yellow.hex),
    BACKGROUND_BLUE(44, ColorClut.Blue.hex),
    BACKGROUND_MAGENTA(45, ColorClut.Magenta.hex),
    BACKGROUND_CYAN(46, ColorClut.Cyan.hex),
    BACKGROUND_WHITE(47, ColorClut.White.hex),
    BACKGROUND_DEFAULT(49, ColorClut.Black.hex);

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