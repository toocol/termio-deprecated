package com.toocol.termio.utilities.escape;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/8 10:45
 */
public class AnisEscapeSearchEngine {
    private static final AnisEscapeSearchEngine instance = new AnisEscapeSearchEngine();

    private static AnisEscapeSearchEngine get() {
        return instance;
    }

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#cursor-controls
    private static final Pattern cursorSetPosModePattern = Pattern.compile("\\u001b\\[\\d{1,4};\\d{1,4}[Hf]");
    private static final Pattern cursorControlModePattern = Pattern.compile("\\u001b[\\[ ]\\d{0,4}([HABCDEFGM78su]|(6n))");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#erase-functions
    private static final Pattern eraseFunctionModePattern = Pattern.compile("\\u001b\\[[0123]?[JK]");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#colors--graphics-mode
    private static final Pattern colorGraphicsModePattern = Pattern.compile("\\u001b\\[((?!38)(?!48)\\d{1,3});(\\d{1,3};)+\\d{1,3}m");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#256-colors
    private static final Pattern color256ModePattern = Pattern.compile("\\u001b\\[(38)?(48)?;5;\\d{1,3}m");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#rgb-colors
    private static final Pattern colorRgbModePattern = Pattern.compile("\\u001b\\[(38)?(48)?;2;\\d{1,3};\\d{1,3};\\d{1,3}m");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#set-mode
    private static final Pattern screenModePatter = Pattern.compile("\\u001b\\[=\\d{1,2}h");
    private static final Pattern disableScreenModePattern = Pattern.compile("\\u001b\\[=\\d{1,2}l");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#common-private-modes
    private static final Pattern commonPrivateMode = Pattern.compile("\\u001b\\[\\?\\d{2,4}[lh]");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#keyboard-strings
    private static final Pattern keyBoardStringMode = Pattern.compile("\\u001b\\[((\\d{1,3};)(\\d{1,3};)([\"\\w ]+;?))+p");

    public Collection<AnisEscapeAction<?>> getEscapeAction(String text) {
        return null;
    }

    private AnisEscapeSearchEngine() {

    }
}
