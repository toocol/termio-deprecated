package com.toocol.termio.utilities.escape;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/7 21:08
 * @version: 0.0.1
 */
public enum EscapeScreenMode implements IEscapeMode {
    MONOCHROME_40_25(0, "40 x 25 monochrome (text)"),
    COLOR_4_40_25(1, "40 x 25 color (4-color text)"),
    MONOCHROME_80_25(2, "80 x 25 monochrome (text)"),
    COLOR_4_80_25(3, "80 x 25 color (4-color text)"),
    COLOR_4_320_200(4, "320 x 200 (4-color graphics)"),
    MONOCHROME_320_200(5, "320 x 200 monochrome (graphics)"),
    MONOCHROME_640_200(6, "640 x 200 monochrome (graphics)"),
    ENABLE_LINE_WRAPPING(7, "Enables line wrapping"),
    COLOR_16_320_200(13, "320 x 200 color (16-color graphics)"),
    COLOR_16_640_200(14, "640 x 200 color (16-color graphics)"),
    MONOCHROME_640_350(15, "640 x 350 monochrome (2-color graphics)"),
    COLOR_16_640_350(16, "640 x 350 color (16-color graphics)"),
    MONOCHROME_640_480(17, "640 x 480 monochrome (2-color graphics)"),
    COLOR_640_480(18, "640 x 480 color (16-color graphics)"),
    COLOR_256_320_200(19, "320 x 200 color (256-color graphics)"),
    ;
    public final int code;
    public final String desc;

    EscapeScreenMode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EscapeScreenMode codeOf(int code) {
        for (EscapeScreenMode mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        return null;
    }
}
