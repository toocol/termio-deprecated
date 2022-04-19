package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.CharUtil;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public class TermReader {

    private final ArrowHelper arrowHelper;
    private final TermHistoryHelper historyHelper ;

    private ConsoleReader reader;

    public TermReader(Term term) {
        arrowHelper = new ArrowHelper();
        historyHelper = new TermHistoryHelper(term);
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
                    Printer.print(String.valueOf(finalChar));
                    lineBuilder.append(finalChar);

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
                } else if(finalChar == CharUtil.LEFT_ARROW || finalChar == CharUtil.RIGHT_ARROW) {

                } else if (finalChar == CharUtil.TAB) {
                    // To fulfill command.
                } else if (finalChar == CharUtil.BACKSPACE) {

                    if (lineBuilder.isEmpty()) {
                        Printer.voice();
                        continue;
                    }
                    lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                    Printer.virtualBackspace();

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
