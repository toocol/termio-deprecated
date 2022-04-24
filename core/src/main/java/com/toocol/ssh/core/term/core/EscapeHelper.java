package com.toocol.ssh.core.term.core;

import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.core.shell.core.Shell;
import jline.console.ConsoleReader;
import jline.internal.NonBlockingInputStream;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/18 22:53
 * @version: 0.0.1
 */
public final class EscapeHelper {

    private boolean acceptEscape = false;
    private boolean acceptBracketAfterEscape = false;

    public char processArrowStream(char inChar) {
        if (inChar == CharUtil.ESCAPE) {
            acceptEscape = true;
        }
        if (acceptEscape) {
            if (inChar == CharUtil.BRACKET_START) {
                acceptBracketAfterEscape = true;
            } else if (inChar != CharUtil.ESCAPE) {
                acceptEscape = false;
            }
        }
        if (acceptBracketAfterEscape && inChar != CharUtil.BRACKET_START) {
            acceptEscape = false;
            acceptBracketAfterEscape = false;
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

    /**
     * To see <a href="https://github.com/jline/jline2/issues/152">jline2/issues/152</a>
     */
    public char processArrowBundle(char inChar, Shell shell, ConsoleReader reader) {
        if (inChar != CharUtil.ESCAPE) {
            return inChar;
        }
        try {
            NonBlockingInputStream stream = (NonBlockingInputStream) reader.getInput();
            // Value -2 is the special code meaning that stream reached its end
            if (stream.peek(100) <= -2) {
                return CharUtil.ESCAPE;
            }

            char inner;
            do {
                inner = (char) reader.readCharacter();
            } while (inner == CharUtil.BRACKET_START);

            switch (inner) {
                case 'A':
                    return CharUtil.UP_ARROW;
                case 'B':
                    return CharUtil.DOWN_ARROW;
                case 'C':
                   return CharUtil.RIGHT_ARROW;
                case 'D':
                    return CharUtil.LEFT_ARROW;
                default:
                    shell.write(CharUtil.ESCAPE);
                    shell.write(CharUtil.BRACKET_START);
                    return inner;
            }

        } catch (Exception e) {
            return inChar;
        }
    }

    public boolean isAcceptBracketAfterEscape() {
        return acceptBracketAfterEscape;
    }

}
