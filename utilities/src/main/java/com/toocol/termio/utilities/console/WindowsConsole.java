package com.toocol.termio.utilities.console;

import com.toocol.termio.utilities.ansi.AsciiControl;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.jni.TermioJNI;
import com.toocol.termio.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/24 11:47
 */
public final class WindowsConsole extends Console {

    private final TermioJNI jni = TermioJNI.getInstance();

    @Override
    public String chooseFiles() {
        return jni.chooseFiles();
    }

    @Override
    public String chooseDirectory() {
        return jni.chooseDirectory();
    }

    @Override
    public int getWindowWidth() {
        int windowWidth;
        try {
            windowWidth = jni.getWindowWidth();
        } catch (Error e) {
            windowWidth = 0;
        }
        return windowWidth;
    }

    @Override
    public int getWindowHeight() {
        int windowHeight;
        try {
            windowHeight = jni.getWindowHeight();
        } catch (Error e) {
            windowHeight = 0;
        }
        return windowHeight;
    }

    @NotNull
    @Override
    public String getCursorPosition() {
        return jni.getCursorPosition();
    }

    @Override
    public void setCursorPosition(int x, int y) {
        jni.setCursorPosition(x, y);
    }

    @Override
    public void cursorBackLine(int lines) {
        jni.cursorBackLine(lines);
    }

    @Override
    public void showCursor() {
        jni.showCursor();
    }

    @Override
    public void hideCursor() {
        jni.hideCursor();
    }

    @Override
    public void cursorLeft() {
        jni.cursorLeft();
    }

    @Override
    public void cursorRight() {
        jni.cursorRight();
    }

    @NotNull
    @Override
    public byte[] cleanUnsupportedCharacter(@NotNull byte[] bytes) {
        return innerClearUnsupportedCharacter(bytes);
    }

    @Override
    public void rollingProcessing(@NotNull String msg) {
        if (AsciiControl.detectRolling(msg)) {
            int cnt = 0;
            for (char ch : msg.toCharArray()) {
                if (ch == '\n') cnt++;
            }
            for (int i = 0; i < cnt; i++) {
                Printer.println("");
            }
        }
    }

    private byte[] innerClearUnsupportedCharacter(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        String msg = new String(bytes, StandardCharsets.UTF_8);
        if (msg.contains("\r\n")) {
            for (String line : msg.split("\r\n")) {
                if (line.contains("\n")) {
                    line = line.replaceAll("\\n{2,}", "\n");
                    for (String sp : line.split("\n")) {
                        sp = doClearString(sp);
                        if (sp != null) {
                            builder.append(sp).append("\n");
                        }
                    }
                } else {
                    line = doClearString(line);
                    if (line != null) {
                        builder.append(line).append("\n");
                    }
                }
            }
        } else {
            if (msg.contains("\n")) {
                msg = msg.replaceAll("\\n{2,}", "\n");
                for (String sp : msg.split("\n")) {
                    sp = doClearString(sp);
                    if (sp != null) {
                        builder.append(sp).append("\n");
                    }
                }
            } else {
                msg = doClearString(msg);
                if (msg != null) {
                    builder.append(msg).append("\n");
                }
            }
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        msg = builder.toString().replaceAll("\\n{2,}", "\n");
        return msg.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * We can ensure that param source doesn't have characters such as '\n','\r\n'
     */
    private String doClearString(String source) {
        if (StringUtils.isEmpty(source)) {
            return StrUtil.EMPTY;
        }
        source = AsciiControl.ignore(source);
        return source;
    }
}
