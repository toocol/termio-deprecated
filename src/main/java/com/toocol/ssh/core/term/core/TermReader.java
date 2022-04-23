package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.common.utils.Tuple2;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public record TermReader(Term term) {

    String readLine() {

        StringBuilder lineBuilder = new StringBuilder();
        Term.executeCursorOldX.set(term.getCursorPosition()._1());
        try {
            while (true) {
                char inChar = (char) term.reader.readCharacter();

                char finalChar = term.escapeHelper.processArrowStream(inChar);

                if (CharUtil.isAsciiPrintable(finalChar)) {

                    if (term.escapeHelper.isAcceptBracketAfterEscape()) {
                        continue;
                    }
                    if (finalChar == CharUtil.SPACE && lineBuilder.length() == 0) {
                        continue;
                    }
                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() >= term.getWidth() - 1) {
                        continue;
                    }
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        int index = cursorPosition._1() - Term.PROMPT.length();
                        if (index == 0 && finalChar == CharUtil.SPACE) {
                            continue;
                        }
                        lineBuilder.insert(index, finalChar);
                    } else {
                        lineBuilder.append(finalChar);
                    }
                    Term.executeCursorOldX.getAndIncrement();
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
                    Term.executeCursorOldX.set(lineBuilder.length() + Term.PROMPT.length());
                } else if (finalChar == CharUtil.LEFT_ARROW || finalChar == CharUtil.RIGHT_ARROW) {
                    int cursorX = term.getCursorPosition()._1();
                    if (finalChar == CharUtil.LEFT_ARROW) {
                        if (cursorX > Term.PROMPT.length()) {
                            term.cursorLeft();
                            Term.executeCursorOldX.getAndDecrement();
                        }
                    } else {
                        if (cursorX < (lineBuilder.length() + Term.PROMPT.length())) {
                            term.cursorRight();
                            Term.executeCursorOldX.getAndIncrement();
                        }
                    }
                    continue;
                } else if (finalChar == CharUtil.TAB) {

                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        term.setCursorPosition(lineBuilder.length() + Term.PROMPT.length(), cursorPosition._2());
                    }
                } else if (finalChar == CharUtil.BACKSPACE) {

                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    if (cursorPosition._1() == Term.PROMPT.length()) {
                        Printer.voice();
                        continue;
                    }
                    if (cursorPosition._1() < lineBuilder.length() + Term.PROMPT.length()) {
                        int index = cursorPosition._1() - Term.PROMPT.length() - 1;
                        lineBuilder.deleteCharAt(index);
                    } else {
                        lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                    }
                    Term.executeCursorOldX.getAndDecrement();

                } else if (finalChar == CharUtil.CTRL_U) {

                    lineBuilder.delete(0, lineBuilder.length());
                    term.clearTermLineWithPrompt();
                    Term.executeCursorOldX.set(Term.PROMPT.length());

                } else if (finalChar == CharUtil.ESCAPE) {
                    continue;
                } else if (finalChar == CharUtil.CR || finalChar == CharUtil.LF) {

                    Tuple2<Integer, Integer> cursorPosition = term.getCursorPosition();
                    term.hideCursor();
                    term.setCursorPosition(Term.PROMPT.length(), cursorPosition._2());
                    term.setCursorPosition(Term.PROMPT.length(), cursorPosition._2());
                    term.showCursor();
                    term.historyHelper.push(lineBuilder.toString());

                    Term.executeCursorOldX.set(Term.PROMPT.length());
                    term.hideCursor();
                    term.printExecution(StrUtil.EMPTY);
                    term.showCursor();
                    return lineBuilder.toString();
                }

                term.hideCursor();
                term.printExecution(lineBuilder.toString());
                term.showCursor();
            }

        } catch (Exception e) {
            Printer.println("\nSomething error.");
            System.exit(-1);
        }
        return null;
    }
}
