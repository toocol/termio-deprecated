package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.Tuple2;

import java.io.IOException;

import static com.toocol.ssh.core.term.core.HighlightHelper.EXECUTE_LINE_BACKGROUND;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public record TermReader(Term term) {

    String readLine() {

        StringBuilder lineBuilder = new StringBuilder();
        try {
            while (true) {
                char inChar = (char) term.reader.readCharacter();

                char finalChar = term.arrowHelper.processArrowStream(inChar);

                if (CharUtil.isAsciiPrintable(finalChar)) {

                    if (term.arrowHelper.isAcceptBracketAfterEscape()) {
                        continue;
                    }
                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        int index = cursorPosition._1() - Term.PROMPT.length();
                        lineBuilder.insert(index, finalChar);
                        term.hideCursor();
                        Printer.print(HighlightHelper.assembleColorBackground(lineBuilder.substring(index, lineBuilder.length()), EXECUTE_LINE_BACKGROUND));
                        term.setCursorPosition(cursorPosition._1() + 1, cursorPosition._2());
                        term.showCursor();
                    } else {
                        lineBuilder.append(finalChar);
                        Printer.print(HighlightHelper.assembleColorBackground(String.valueOf(finalChar), EXECUTE_LINE_BACKGROUND));
                    }

                } else if (finalChar == CharUtil.UP_ARROW || finalChar == CharUtil.DOWN_ARROW) {
                    if (finalChar == CharUtil.UP_ARROW) {
                        if (!term.historyHelper.isStart()) {
                            if (lineBuilder.toString().length() != 0) {
                                term.historyHelper.pushToDown(lineBuilder.toString());
                            }
                        }
                        String up = term.historyHelper.up();
                        if (up != null) {
                            lineBuilder.delete(0, lineBuilder.length()).append(up);
                        }
                    } else {
                        String down = term.historyHelper.down();
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
                        Printer.virtualBackspaceWithBackground(EXECUTE_LINE_BACKGROUND);
                        Printer.print(HighlightHelper.assembleColorBackground(lineBuilder.substring(index, lineBuilder.length()) + CharUtil.SPACE, EXECUTE_LINE_BACKGROUND));
                        term.setCursorPosition(cursorPosition._1() - 1, cursorPosition._2());
                        term.showCursor();
                    } else {
                        lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                        Printer.virtualBackspaceWithBackground(EXECUTE_LINE_BACKGROUND);
                    }

                } else if (finalChar == CharUtil.CR || finalChar == CharUtil.LF) {

                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    term.hideCursor();
                    term.setCursorPosition(Term.PROMPT.length(), cursorPosition._2());
                    Printer.print(HighlightHelper.assembleColorBackground(" ".repeat(lineBuilder.length()), EXECUTE_LINE_BACKGROUND));
                    term.setCursorPosition(Term.PROMPT.length(), cursorPosition._2());
                    term.showCursor();
                    term.historyHelper.push(lineBuilder.toString());
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
}
