package com.toocol.termio.utilities.escape

import com.toocol.termio.utilities.ansi.ColorClut

enum class EscapeColorISOMode(val colorCode: Int, val hexCode: String, val foreground: Boolean) : IEscapeMode {
    FOREGROUND_BRIGHT_BLACK(90, ColorClut.Black.hex, true),
    FOREGROUND_BRIGHT_RED(91, ColorClut.Red.hex, true),
    FOREGROUND_BRIGHT_GREEN(92, ColorClut.Green.hex, true),
    FOREGROUND_BRIGHT_YELLOW(93, ColorClut.Yellow.hex, true),
    FOREGROUND_BRIGHT_BLUE(94, ColorClut.Blue.hex, true),
    FOREGROUND_BRIGHT_MAGENTA(95, ColorClut.Magenta.hex, true),
    FOREGROUND_BRIGHT_CYAN(96, ColorClut.Cyan.hex, true),
    FOREGROUND_BRIGHT_WHITE(97, ColorClut.White.hex, true),
    BACKGROUND_BRIGHT_BLACK(100, ColorClut.Black.hex, false),
    BACKGROUND_BRIGHT_RED(101, ColorClut.Red.hex, false),
    BACKGROUND_BRIGHT_GREEN(102, ColorClut.Green.hex, false),
    BACKGROUND_BRIGHT_YELLOW(103, ColorClut.Yellow.hex, false),
    BACKGROUND_BRIGHT_BLUE(104, ColorClut.Blue.hex, false),
    BACKGROUND_BRIGHT_MAGENTA(105, ColorClut.Magenta.hex, false),
    BACKGROUND_BRIGHT_CYAN(106, ColorClut.Cyan.hex, false),
    BACKGROUND_BRIGHT_WHITE(107, ColorClut.White.hex, false);

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