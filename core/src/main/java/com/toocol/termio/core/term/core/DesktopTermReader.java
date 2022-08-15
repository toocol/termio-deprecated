package com.toocol.termio.core.term.core;

import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.CharUtil;
import com.toocol.termio.utilities.utils.MessageBox;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public record DesktopTermReader(Term term) implements ITermReader {

    @Override
    @SuppressWarnings("all")
    public String readLine() {
        term.executeCursorOldX.set(term.getCursorPosition()[0]);
        try {
            while (true) {
                char inChar = (char) term.reader.readCharacter();
                char finalChar = term.escapeHelper.processArrowBundle(inChar, term.reader);

                if (term.termCharEventDispatcher.dispatch(term, finalChar)) {
                    String cmd = term.lineBuilder.toString();
                    term.lineBuilder.delete(0, term.lineBuilder.length());
                    term.lastChar = finalChar;
                    return cmd;
                }

                term.lastChar = finalChar;
                Printer.print("" + finalChar);
            }

        } catch (Exception e) {
            MessageBox.setExitMessage("Term reader error.");
            System.exit(-1);
        }
        return null;
    }
}
