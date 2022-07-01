package com.toocol.ssh.core.term.core;

import com.toocol.ssh.core.term.handlers.DynamicEchoHandler;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.ExitMessage;
import com.toocol.ssh.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.term.TermAddress.TERMINAL_ECHO;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public record TermReader(Term term) {

    @SuppressWarnings("all")
    String readLine() {
        term.executeCursorOldX.set(term.getCursorPosition()[0]);
        try {
            while (true) {
                char inChar = (char) term.reader.readCharacter();
                char finalChar = term.escapeHelper.processArrowStream(inChar);

                if (term.termCharEventDispatcher.dispatch(term, finalChar)) {
                    String cmd = term.lineBuilder.toString();
                    term.lineBuilder.delete(0, term.lineBuilder.length());
                    if (StringUtils.isEmpty(cmd) && term.lastChar != CharUtil.CR) {
                        term.eventBus.send(TERMINAL_ECHO.address(), StrUtil.EMPTY);
                    }
                    term.lastChar = finalChar;
                    DynamicEchoHandler.lastInput = StrUtil.EMPTY;
                    return cmd;
                }

                term.lastChar = finalChar;
                term.printExecution(term.lineBuilder.toString());
                term.eventBus.send(TERMINAL_ECHO.address(), term.lineBuilder.toString());
            }

        } catch (Exception e) {
            ExitMessage.setMsg("Term reader error.");
            System.exit(-1);
        }
        return null;
    }
}
