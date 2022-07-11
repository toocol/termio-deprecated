package com.toocol.ssh.utilities.console;

import com.toocol.ssh.utilities.jni.TermioJNI;
import com.toocol.ssh.utilities.utils.AsciiControl;
import com.toocol.ssh.utilities.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

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

    @Override
    public byte[] cleanUnsupportedCharacter(byte[] bytes) {
        return innerClearUnsupportedCharacter(bytes);
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
                        if (StringUtils.isNotEmpty(sp)) {
                            builder.append(sp).append("\n");
                        }
                    }
                } else {
                    line = doClearString(line);
                    if (StringUtils.isNotEmpty(line)) {
                        builder.append(line).append("\n");
                    }
                }
            }
        } else {
            if (msg.contains("\n")) {
                msg = msg.replaceAll("\\n{2,}", "\n");
                for (String sp : msg.split("\n")) {
                    sp = doClearString(sp);
                    if (StringUtils.isNotEmpty(sp)) {
                        builder.append(sp).append("\n");
                    }
                }
            } else {
                msg = doClearString(msg);
                if (StringUtils.isNotEmpty(msg)) {
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
        if (AsciiControl.haveUnsupportedAsciiControl(source)) {
            int preLen = source.length();
            source = AsciiControl.preClear(source);
            if (source.length() != preLen) {
                if (AsciiControl.haveEscape(source)) {
                    source = AsciiControl.escapeMatch(source);
                }
            } else if (AsciiControl.haveEscape(source)) {
                source = source.substring(source.indexOf(AsciiControl.ESCAPE));
            } else if (AsciiControl.haveBs(source)) {
                source = AsciiControl.bsMatch(source);
            } else {
                source = StrUtil.EMPTY;
            }
        } else if (AsciiControl.haveEscape(source)) {
            source = AsciiControl.escapeMatch(source);
        }
        source = AsciiControl.ignoreAndReplace(source);
        if (source.equals(AsciiControl.ESCAPE)) {
            source = StrUtil.EMPTY;
        }
        return source;
    }
}
