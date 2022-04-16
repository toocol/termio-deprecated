package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TerminatioJNI;
import com.toocol.ssh.common.utils.CharUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/16 15:23
 */
public class TermReader {

    private static final TerminatioJNI JNI = TerminatioJNI.getInstance();

    public String readLine() {
        return readLine(null);
    }

    public String readLine(String prompt) {
        if (StringUtils.isNotEmpty(prompt)) {
            Printer.print(prompt);
        }

        StringBuilder lineBuilder = new StringBuilder();
        while (true) {
            char ch = (char) JNI.getCh();
            if (CharUtil.isAsciiPrintable(ch)) {
                Printer.print(String.valueOf(ch));
                lineBuilder.append(ch);
            } else if (ch == CharUtil.TAB) {
                // TODO:
            } else if (ch == CharUtil.BACKSPACE) {
                if (lineBuilder.isEmpty()) {
                    Printer.voice();
                    continue;
                }
                lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                Printer.virtualBackspace();
            } else if (ch == CharUtil.CR || ch == CharUtil.LF) {
                Printer.println();
                break;
            }
        }
        return lineBuilder.toString();
    }
}
