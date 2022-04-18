package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.CharUtil;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public class TermReader {

    private final ArrowHelper arrowHelper = new ArrowHelper();

    private ConsoleReader reader;

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

        AtomicBoolean acceptEscape = new AtomicBoolean();
        AtomicBoolean acceptBracketsAfterEscape = new AtomicBoolean();

        StringBuilder lineBuilder = new StringBuilder();
        try {
            while (true) {
                char inChar = (char) reader.readCharacter();

                char finalChar = arrowHelper.processArrow(inChar, acceptEscape, acceptBracketsAfterEscape);

                if (CharUtil.isAsciiPrintable(finalChar)) {

                    if (acceptBracketsAfterEscape.get()) {
                        continue;
                    }
                    Printer.print(String.valueOf(finalChar));
                    lineBuilder.append(finalChar);

                } else if (finalChar == CharUtil.TAB) {

                    System.out.print("tab");

                } else if (finalChar == CharUtil.BACKSPACE) {

                    if (lineBuilder.isEmpty()) {
                        Printer.voice();
                        continue;
                    }
                    lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                    Printer.virtualBackspace();

                } else if (finalChar == CharUtil.CR || finalChar == CharUtil.LF) {

                    Printer.println();
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
