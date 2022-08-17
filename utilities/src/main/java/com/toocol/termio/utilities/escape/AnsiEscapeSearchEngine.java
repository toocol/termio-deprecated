package com.toocol.termio.utilities.escape;

import com.google.common.collect.ImmutableMap;
import com.toocol.termio.utilities.escape.actions.AnsiEscapeAction;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.Castable;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/8 10:45
 */
public class AnsiEscapeSearchEngine<T extends EscapeCodeSequenceSupporter<T>> implements Loggable, Castable {

    protected final T executeTarget;

    private Map<Class<? extends IEscapeMode>, AnsiEscapeAction<T>> actionMap;

    private static final Pattern wordNumberPattern = Pattern.compile("\\w+");
    private static final Pattern numberPattern = Pattern.compile("\\d+");
    private static final Pattern wordPattern = Pattern.compile("[a-zA-Z]+");
    private static final Pattern codeStringPattern = Pattern.compile("(\\d{1,3};){1,2}[\\\\\"'\\w ]+;?");
    private static final Pattern codePattern = Pattern.compile("(\\d{1,3};)+");
    private static final Pattern stringPattern = Pattern.compile("[\\w ]+;?");

    private static final String uberEscapeModeRegex =
            "(\\u001b\\[\\d{1,4};\\d{1,4}[Hf])" +
                    "|((\\u001b\\[\\d{0,4}([HABCDEFGsu]|(6n)))|(\\u001b [M78]))" +
                    "|(\\u001b\\[[0123]?[JK])" +
                    "|(\\u001b\\[((?!38)(?!48)\\d{1,3};?)+m)" +
                    "|(\\u001b\\[(38)?(48)?;5;\\d{1,3}m)" +
                    "|(\\u001b\\[(38)?(48)?;2;\\d{1,3};\\d{1,3};\\d{1,3}m)" +
                    "|(\\u001b\\[=\\d{1,2}h)" +
                    "|(\\u001b\\[=\\d{1,2}l)" +
                    "|(\\u001b\\[\\?\\d{2,4}[lh])" +
                    "|(\\u001b\\[((\\d{1,3};){1,2}(((\\\\\")|'|\")[\\w ]+((\\\\\")|'|\");?)|(\\d{1,2};?))+p)";
    private static final Pattern uberEscapeModePattern = Pattern.compile(uberEscapeModeRegex);

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

    /**
     * Suppose we have such text:<br>
     * ######ESC[K######<br>
     * |<br>
     * | split text by escape sequence, generate escape sequence queue.<br>
     * ↓<br>
     * ["######", "######"]<br>
     * Queue[Tuple[IEscapeMode,List[Object]]]<br><br>
     * Then we print out the split text in loop, and getting and invoking AnsiEscapeAction from the head of Queue[Tuple[IEscapeMode,List[Object]]].
     */
    public void actionOnEscapeMode(String text) {
        if (actionMap == null) {
            warn("AnsiEscapeSearchEngine is not initialized.");
            return;
        }

        Queue<Tuple2<IEscapeMode, List<Object>>> queue = new ArrayDeque<>();
        String[] split = text.split(uberEscapeModeRegex);
        Matcher uberMatcher = uberEscapeModePattern.matcher(text);
        while (uberMatcher.find()) {
            String escapeSequence = uberMatcher.group();
            List<Object> params = new ArrayList<>();
            IEscapeMode escapeMode = null;

            AtomicBoolean dealAlready = new AtomicBoolean();
            regexParse(escapeSequence, cursorSetPosModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, cursorControlModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, eraseFunctionModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, colorGraphicsModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, color256ModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, colorRgbModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, screenModePatter, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, disableScreenModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, commonPrivateModePattern, matcher -> {

            }, dealAlready);

            regexParse(escapeSequence, keyBoardStringModePattern, matcher -> {

            }, dealAlready);

            queue.offer(new Tuple2<>(escapeMode, params));
        }

        for (String sp : split) {
            if (StrUtil.isNotEmpty(sp)) {
                executeTarget.printOut(sp);
            }
            if (!queue.isEmpty()) {
                Tuple2<IEscapeMode, List<Object>> tuple = queue.poll();
                actionMap.get(tuple._1().getClass()).action(executeTarget, tuple._1(), tuple._2());
            }
        }
    }

    private void regexParse(String text, Pattern pattern, Consumer<Matcher> consumer, AtomicBoolean dealAlready) {
        if (dealAlready.get()) {
            return;
        }
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            consumer.accept(matcher);
            dealAlready.set(true);
        }
    }

    private void registerActions(List<AnsiEscapeAction<T>> actions) {
        Map<Class<? extends IEscapeMode>, AnsiEscapeAction<T>> map = new HashMap<>();
        for (AnsiEscapeAction<T> action : actions) {
            map.put(action.focusMode(), action);
        }
        actionMap = ImmutableMap.copyOf(map);
    }

    public AnsiEscapeSearchEngine(T executeTarget) {
        this.executeTarget = executeTarget;
        registerActions(executeTarget.registerActions());
    }
}
