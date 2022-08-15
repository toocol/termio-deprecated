package com.toocol.termio.core.term.core;

import com.toocol.termio.core.term.handlers.console.DynamicEchoHandler;
import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.MessageBox;
import com.toocol.termio.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.termio.core.term.TermAddress.TERMINAL_ECHO;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public record ConsoleTermReader(Term term) implements ITermReader {

    @Override
    @SuppressWarnings("all")
    public String readLine() {
        term.executeCursorOldX.set(term.getCursorPosition()[0]);
        try {
            while (true) {
                char inChar = (char) term.reader.readCharacter();
                char finalChar = term.escapeHelper.processArrowBundle(inChar,term.reader);

                if (term.status.equals(TermStatus.HISTORY_OUTPUT) && !CharUtil.isLeftOrRightArrow(finalChar) && finalChar != '\u001b') {
                    continue;
                }

                if (term.termCharEventDispatcher.dispatch(term, finalChar)) {
                    String cmd = term.lineBuilder.toString();
                    term.lineBuilder.delete(0, term.lineBuilder.length());
                    if (StringUtils.isEmpty(cmd) && term.lastChar != CharUtil.CR) {
                        term.eventBus().send(TERMINAL_ECHO.address(), StrUtil.EMPTY);
                    }
                    term.lastChar = finalChar;
                    DynamicEchoHandler.lastInput = StrUtil.EMPTY;
                    return cmd;
                }

                if (term.status.equals(TermStatus.HISTORY_OUTPUT)) {
                    continue;
                }

                term.lastChar = finalChar;
                term.printExecution(term.lineBuilder.toString());

                term.eventBus().send(TERMINAL_ECHO.address(), term.lineBuilder.toString());
            }

        } catch (Exception e) {
            MessageBox.setExitMessage("Term reader error.");
            System.exit(-1);
        }
        return null;
    }
}
