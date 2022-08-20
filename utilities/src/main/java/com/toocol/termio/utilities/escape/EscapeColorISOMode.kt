package com.toocol.termio.utilities.escape;

import java.util.HashMap;
import java.util.Map;

import static com.toocol.termio.utilities.ansi.ColorClut.*;

public enum EscapeColorISOMode implements IEscapeMode {
    FOREGROUND_BRIGHT_BLACK(90, Black.hex),
    FOREGROUND_BRIGHT_RED(91, Red.hex),
    FOREGROUND_BRIGHT_GREEN(92, Green.hex),
    FOREGROUND_BRIGHT_YELLOW(93, Yellow.hex),
    FOREGROUND_BRIGHT_BLUE(94, Blue.hex),
    FOREGROUND_BRIGHT_MAGENTA(95, Magenta.hex),
    FOREGROUND_BRIGHT_CYAN(96, Cyan.hex),
    FOREGROUND_BRIGHT_WHITE(97, White.hex),
    BACKGROUND_BRIGHT_BLACK(100, Black.hex),
    BACKGROUND_BRIGHT_RED(101, Red.hex),
    BACKGROUND_BRIGHT_GREEN(102, Green.hex),
    BACKGROUND_BRIGHT_YELLOW(103, Yellow.hex),
    BACKGROUND_BRIGHT_BLUE(104, Blue.hex),
    BACKGROUND_BRIGHT_MAGENTA(105, Magenta.hex),
    BACKGROUND_BRIGHT_CYAN(106, Cyan.hex),
    BACKGROUND_BRIGHT_WHITE(107, White.hex),
    ;
    private static final Map<Integer, String> colorHexMap = new HashMap<>();

    static {
        for (EscapeColorISOMode color : values()) {
            colorHexMap.put(color.colorCode, color.hexCode);
        }
    }

    public final int colorCode;
    public final String hexCode;

    public static EscapeColorISOMode codeOf(int code) {
        for (EscapeColorISOMode value : values()) {
            if (value.colorCode == code) {
                return value;
            }
        }
        return null;
    }


    EscapeColorISOMode(int colorCode, String hexCode) {
        this.colorCode = colorCode;
        this.hexCode = hexCode;
    }

    public static String hexOf(int colorCode) {
        return colorHexMap.get(colorCode);
    }
}