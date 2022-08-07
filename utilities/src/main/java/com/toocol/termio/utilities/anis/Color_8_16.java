package com.toocol.termio.utilities.anis;

import java.util.HashMap;
import java.util.Map;

import static com.toocol.termio.utilities.anis.ColorClut.*;

enum Color_8_16 {
    FOREGROUND_BLACK(30, Black.hex),
    FOREGROUND_RED(31, Red.hex),
    FOREGROUND_GREEN(32, Green.hex),
    FOREGROUND_YELLOW(33, Yellow.hex),
    FOREGROUND_BLUE(34, Blue.hex),
    FOREGROUND_MAGENTA(35, Magenta.hex),
    FOREGROUND_CYAN(36, Cyan.hex),
    FOREGROUND_WHITE(37, White.hex),
    FOREGROUND_DEFAULT(39, White.hex),
    BACKGROUND_BLACK(40, Black.hex),
    BACKGROUND_RED(41, Red.hex),
    BACKGROUND_GREEN(42, Green.hex),
    BACKGROUND_YELLOW(43, Yellow.hex),
    BACKGROUND_BLUE(44, Blue.hex),
    BACKGROUND_MAGENTA(45, Magenta.hex),
    BACKGROUND_CYAN(46, Cyan.hex),
    BACKGROUND_WHITE(47, White.hex),
    BACKGROUND_DEFAULT(49, Black.hex),
    ;
    private static final Map<Integer, String> colorHexMap = new HashMap<>();

    static {
        for (Color_8_16 color : values()) {
            colorHexMap.put(color.colorCode, color.hexCode);
        }
    }

    public final int colorCode;
    public final String hexCode;

    Color_8_16(int colorCode, String hexCode) {
        this.colorCode = colorCode;
        this.hexCode = hexCode;
    }

    public static String hexOf(int colorCode) {
        return colorHexMap.get(colorCode);
    }
}
