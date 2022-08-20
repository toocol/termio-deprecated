package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.ansi.ColorClut

enum class EscapeColorISOMode(val colorCode: Int, val hexCode: String) : IEscapeMode {
    FOREGROUND_BRIGHT_BLACK(90, ColorClut.Black.hex),
    FOREGROUND_BRIGHT_RED(91, ColorClut.Red.hex),
    FOREGROUND_BRIGHT_GREEN(92, ColorClut.Green.hex),
    FOREGROUND_BRIGHT_YELLOW(93, ColorClut.Yellow.hex),
    FOREGROUND_BRIGHT_BLUE(94, ColorClut.Blue.hex),
    FOREGROUND_BRIGHT_MAGENTA(95, ColorClut.Magenta.hex),
    FOREGROUND_BRIGHT_CYAN(96, ColorClut.Cyan.hex),
    FOREGROUND_BRIGHT_WHITE(97, ColorClut.White.hex),
    BACKGROUND_BRIGHT_BLACK(100, ColorClut.Black.hex),
    BACKGROUND_BRIGHT_RED(101, ColorClut.Red.hex),
    BACKGROUND_BRIGHT_GREEN(102, ColorClut.Green.hex),
    BACKGROUND_BRIGHT_YELLOW(103, ColorClut.Yellow.hex),
    BACKGROUND_BRIGHT_BLUE(104, ColorClut.Blue.hex),
    BACKGROUND_BRIGHT_MAGENTA(105, ColorClut.Magenta.hex),
    BACKGROUND_BRIGHT_CYAN(106, ColorClut.Cyan.hex),
    BACKGROUND_BRIGHT_WHITE(107, ColorClut.White.hex);

    companion object {
        private val colorHexMap: MutableMap<Int, String> = HashMap()

        init {
            for (color in values()) {
                colorHexMap[color.colorCode] = color.hexCode
            }
        }

        fun codeOf(code: Int): EscapeColorISOMode? {
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