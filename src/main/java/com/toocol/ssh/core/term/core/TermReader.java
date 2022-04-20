package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.Tuple2;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public class TermReader {

    private final Term term;
    private final ArrowHelper arrowHelper;
    private final TermHistoryHelper historyHelper;

    private ConsoleReader reader;

    public TermReader(Term term) {
        arrowHelper = new ArrowHelper();
        historyHelper = new TermHistoryHelper(term);
        this.term = term;
    }

    {
        try {
            reader = new ConsoleReader();
        } catch (Exception e) {
            Printer.println("\nCreate console reader failed.");
            System.exit(-1);
        }
    }

    public String readLine(String prompt) {
        if (StringUtils.isNotEmpty(prompt)) {
            Printer.print(prompt);
        }

        StringBuilder lineBuilder = new StringBuilder();
        try {
            while (true) {
                char inChar = (char) reader.readCharacter();

                char finalChar = arrowHelper.processArrowStream(inChar);

                if (CharUtil.isAsciiPrintable(finalChar)) {

                    if (arrowHelper.isAcceptBracketAfterEscape()) {
                        continue;
                    }
                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        int index = cursorPosition._1() - Term.PROMPT.length();
                        lineBuilder.insert(index, finalChar);
                        term.hideCursor();
                        Printer.print(lineBuilder.substring(index, lineBuilder.length()));
                        term.setCursorPosition(cursorPosition._1() + 1, cursorPosition._2());
                        term.showCursor();
                    } else {
                        lineBuilder.append(finalChar);
                        Printer.print(String.valueOf(finalChar));
                    }

                } else if (finalChar == CharUtil.UP_ARROW || finalChar == CharUtil.DOWN_ARROW) {
                    if (finalChar == CharUtil.UP_ARROW) {
                        if (!historyHelper.isStart()) {
                            if (lineBuilder.toString().length() != 0) {
                                historyHelper.pushToDown(lineBuilder.toString());
                            }
                        }
                        String up = historyHelper.up();
                        if (up != null) {
                            lineBuilder.delete(0, lineBuilder.length()).append(up);
                        }
                    } else {
                        String down = historyHelper.down();
                        if (down != null) {
                            lineBuilder.delete(0, lineBuilder.length()).append(down);
                        }
                    }
                } else if (finalChar == CharUtil.LEFT_ARROW || finalChar == CharUtil.RIGHT_ARROW) {
                    int cursorX = term.getCursorPosition()._1();
                    if (finalChar == CharUtil.LEFT_ARROW) {
                        if (cursorX > Term.PROMPT.length()) {
                            term.cursorLeft();
                        }
                    } else {
                        if (cursorX < (lineBuilder.length() + Term.PROMPT.length())) {
                            term.cursorRight();
                        }
                    }
                } else if (finalChar == CharUtil.TAB) {
                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        term.setCursorPosition(lineBuilder.length() + Term.PROMPT.length(), cursorPosition._2());
                    }
                } else if (finalChar == CharUtil.BACKSPACE) {

                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() == Term.PROMPT.length()){
                        Printer.voice();
                        continue;
                    }
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        int index = cursorPosition._1() - Term.PROMPT.length() - 1;
                        lineBuilder.deleteCharAt(index);
                        term.hideCursor();
                        Printer.virtualBackspace();
                        Printer.print(lineBuilder.substring(index, lineBuilder.length()) + CharUtil.SPACE);
                        term.setCursorPosition(cursorPosition._1() - 1, cursorPosition._2());
                        term.showCursor();
                    } else {
                        lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                        Printer.virtualBackspace();
                    }

                } else if (finalChar == CharUtil.CR || finalChar == CharUtil.LF) {

                    Printer.println();
                    historyHelper.push(lineBuilder.toString());
                    break;

                }
            }
            return lineBuilder.toString();
        } catch (IOException e) {
            Printer.println("\nIO error.");
            System.exit(-1);
        }
        return null;
    }

    public ConsoleReader getReader() {
        return reader;
    }
}
