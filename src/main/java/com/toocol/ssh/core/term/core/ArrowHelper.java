package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.CharUtil;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/18 22:53
 * @version: 0.0.1
 */
public class ArrowHelper {

    public char processArrow(char inChar, AtomicBoolean acceptEscape, AtomicBoolean acceptBracketsAfterEscape) {
        if (inChar == '\u001B') {
            acceptEscape.set(true);
        }
        if (acceptEscape.get()) {
            if (inChar == '[') {
                acceptBracketsAfterEscape.set(true);
            } else if (inChar != '\u001B') {
                acceptEscape.set(false);
            }
        }
        if (acceptBracketsAfterEscape.get() && inChar != '[') {
            acceptEscape.set(false);
            acceptBracketsAfterEscape.set(false);
            inChar = switch (inChar) {
                case 'A' -> CharUtil.UP_ARROW;
                case 'B' -> CharUtil.DOWN_ARROW;
                case 'C' -> CharUtil.RIGHT_ARROW;
                case 'D' -> CharUtil.LEFT_ARROW;
                default -> inChar;
            };
        }
        return inChar;
    }

}
