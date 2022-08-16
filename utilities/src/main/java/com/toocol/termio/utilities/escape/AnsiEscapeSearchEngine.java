package com.toocol.termio.utilities.escape;

import com.google.common.collect.ImmutableMap;
import com.toocol.termio.utilities.ansi.AsciiControl;
import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.Castable;
import com.toocol.termio.utilities.utils.Tuple2;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/8 10:45
 */
public class AnsiEscapeSearchEngine<T extends EscapeCodeSequenceSupporter> implements Loggable, Castable {
    private static final String ESCAPE_SEQUENCE_PLACEHOLDER = AsciiControl.SUB;

    protected final T executeTarget;

    private Map<Class<? extends IEscapeMode>, AnsiEscapeAction<T>> actionMap;

    private static final Pattern wordNumberPattern = Pattern.compile("\\w+");
    private static final Pattern numberPattern = Pattern.compile("\\d+");
    private static final Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
    private static final Pattern codeStringPattern = Pattern.compile("(\\d{1,3};){1,2}[\\\\\"'\\w ]+;?");
    private static final Pattern codePattern = Pattern.compile("(\\d{1,3};)+");
    private static final Pattern stringPattern = Pattern.compile("[\\w ]+;?");

    private static final Pattern uberEscapeModePattern = Pattern.compile(
            "(\\u001b\\[\\d{1,4};\\d{1,4}[Hf])" +
                    "|((\\u001b\\[\\d{0,4}([HABCDEFGsu]|(6n)))|(\\u001b [M78]))" +
                    "|(\\u001b\\[[0123]?[JK])" +
                    "|(\\u001b\\[((?!38)(?!48)\\d{1,3};?)+m)" +
                    "|(\\u001b\\[(38)?(48)?;5;\\d{1,3}m)" +
                    "|(\\u001b\\[(38)?(48)?;2;\\d{1,3};\\d{1,3};\\d{1,3}m)" +
                    "|(\\u001b\\[=\\d{1,2}h)" +
                    "|(\\u001b\\[=\\d{1,2}l)" +
                    "|(\\u001b\\[\\?\\d{2,4}[lh])" +
                    "|(\\u001b\\[((\\d{1,3};){1,2}(((\\\\\")|'|\")[\\w ]+((\\\\\")|'|\");?)|(\\d{1,2};?))+p)"
    );

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#cursor-controls
    private static final Pattern cursorSetPosModePattern = Pattern.compile("\\u001b\\[\\d{1,4};\\d{1,4}[Hf]");
    private static final Pattern cursorControlModePattern = Pattern.compile("(\\u001b\\[\\d{0,4}([HABCDEFGsu]|(6n)))|(\\u001b [M78])");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#erase-functions
    private static final Pattern eraseFunctionModePattern = Pattern.compile("\\u001b\\[[0123]?[JK]");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#colors--graphics-mode
    private static final Pattern colorGraphicsModePattern = Pattern.compile("\\u001b\\[((?!38)(?!48)\\d{1,3};?)+m");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#256-colors
    private static final Pattern color256ModePattern = Pattern.compile("\\u001b\\[(38)?(48)?;5;\\d{1,3}m");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#rgb-colors
    private static final Pattern colorRgbModePattern = Pattern.compile("\\u001b\\[(38)?(48)?;2;\\d{1,3};\\d{1,3};\\d{1,3}m");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#set-mode
    private static final Pattern screenModePatter = Pattern.compile("\\u001b\\[=\\d{1,2}h");
    private static final Pattern disableScreenModePattern = Pattern.compile("\\u001b\\[=\\d{1,2}l");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#common-private-modes
    private static final Pattern commonPrivateModePattern = Pattern.compile("\\u001b\\[\\?\\d{2,4}[lh]");

    // see: https://gist.github.com/Joezeo/ce688cf42636376650ead73266256336#keyboard-strings
    private static final Pattern keyBoardStringModePattern = Pattern.compile("\\u001b\\[((\\d{1,3};){1,2}(((\\\\\")|'|\")[\\w ]+((\\\\\")|'|\");?)|(\\d{1,2};?))+p");

    public void registerActions(List<AnsiEscapeAction<T>> actions) {
        Map<Class<? extends IEscapeMode>, AnsiEscapeAction<T>> map = new HashMap<>();
        for (AnsiEscapeAction<T> action : actions) {
            map.put(action.focusMode(), action);
        }
        actionMap = ImmutableMap.copyOf(map);
    }

    /**
     * Suppose we have such text:<br>
     * ######ESC[K######<br>
     * |<br>
     * |to splitText<br>
     * ↓<br>
     * ["######",IEscapeMode,"######"]<br>
     * Meanwhile we record an AnsiEscapeAction that represent an origin escape code sequence to a Queue[AnsiEscapeAction]<br>
     * Then we print out the splitText in loop, and invoking AnsiEscapeAction from the head of Queue[AnsiEscapeAction] when we meet the "SUB".
     */
    public void actionOnEscapeMode(String text) {
        if (actionMap == null) {
            warn("AnsiEscapeSearchEngine is not initialized.");
            return;
        }

        String copy = text;
        List<Object> splitText = new ArrayList<>();
        Queue<AnsiEscapeAction<T>> queue = new ArrayDeque<>();
        Matcher uberMatcher = uberEscapeModePattern.matcher(text);
        while (uberMatcher.find()) {
            int start = uberMatcher.start();
            int end = uberMatcher.end();
            // TODO: fulfil the message
        }

        for (Object sp : splitText) {
            if (sp instanceof Tuple2<?, ?>) {
                Tuple2<IEscapeMode, List<Object>> tuple = cast(sp);
                queue.peek().action(executeTarget, tuple._1(), tuple._2());
                continue;
            }
            executeTarget.printOut(sp.toString());
        }
    }

    private void regexParse(String text, Pattern pattern, Consumer<Matcher> consumer) {
        consumer.accept(pattern.matcher(text));
    }

    public AnsiEscapeSearchEngine(T executeTarget) {
        this.executeTarget = executeTarget;
    }
}
