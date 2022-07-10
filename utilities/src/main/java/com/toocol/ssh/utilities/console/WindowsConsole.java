package com.toocol.ssh.utilities.console;

import com.toocol.ssh.utilities.jni.TermioJNI;
import com.toocol.ssh.utilities.utils.AnisControl;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.StrUtil;

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
        String msg = new String(bytes, StandardCharsets.UTF_8);
        if ((msg.startsWith("\n\u0004:\u0002@") && msg.length() == 6)
                || (msg.startsWith("\u0004:\u0002@") && msg.length() == 5)) {
            msg = StrUtil.EMPTY;
        }
        if (msg.contains("\u0005:\u0003@�") && !msg.contains("\r\n")) {
            msg = StrUtil.EMPTY;
        }
        if (msg.contains("\u0016\u0012\u0014\"\u0012")) {
            msg = StrUtil.EMPTY;
        }
        if (msg.contains(AnisControl.BS) && msg.contains(AnisControl.FF_DC2)) {
            msg = msg.substring(msg.indexOf(CharUtil.BACKSPACE));
        }
        msg = msg.replaceAll("\\u001B\\[\\?25h", StrUtil.EMPTY);
        msg = msg.replaceAll("\\u001A\\u0005\\(�\\u000102", AnisControl.DC2);
        msg = msg.replaceAll("�\\b\\u0012�\\b\"�\\b", AnisControl.DC2);
        msg = msg.replaceAll("\\u0012Z\"X", AnisControl.DC2);
        msg = msg.replaceAll(";\\u00129\"7ls\\u001B", AnisControl.DC2);
        msg = msg.replaceAll("\\)\\u0012", AnisControl.DC2);
        msg = msg.replaceAll("�\\u0002\\u0012�\\u0002\"�\\u0002ls\\u001B\\[\\?25l", AnisControl.DELETE_LINE);
        msg = msg.replaceAll("\\u0005:\\u0003@�\\u0001", AnisControl.DELETE_LINE);
        msg = msg.replaceAll("[a-z]\\u0012", AnisControl.DC2);
        msg = msg.replaceAll("[A-Z]\\u0012", AnisControl.DELETE_LINE);
        msg = msg.replaceAll("\\+\\u0012", AnisControl.DELETE_LINE);
        msg = msg.replaceAll("�\\u0003\\u0012�\\u0003\"�\\u0003ls", AnisControl.DELETE_LINE);
        for (int i = 0; i < 32; i++) {
            String regex = "\\u000" + i + "\\u0012";
            msg = msg.replaceAll(regex, AnisControl.DC2);

            regex = "�\\u000" + i + "\\u0012�\\u000" + i + "\"�\\u000" + i;
            if (msg.contains(AnisControl.ESCAPE)) {
                msg = msg.replaceAll(regex, AnisControl.DC2);
            } else {
                msg = msg.replaceAll(regex, "");
            }
        }

        StringBuilder builder = new StringBuilder();
        String[] split = msg.split(StrUtil.CRLF);
        if (split.length <= 1) {
            split = msg.split(StrUtil.LF);
        }
        if (split.length <= 1) {
            split = msg.split(StrUtil.CR);
        }
        for (int i = 0; i < split.length; i++) {
            String sp = split[i];
            if (sp.contains(AnisControl.EOT_STX)) {
                continue;
            }
            if (sp.contains(AnisControl.FF_DC2)) {
                continue;
            }
            if (sp.equals("\n")) {
                continue;
            }
            if (sp.contains("\u0005:\u0003@�")) {
                if (sp.contains("�\u0002\u0012�\u0002\"�\u0002")) {
                    sp = sp.substring(sp.lastIndexOf(AnisControl.STX) + 1);
                } else continue;
            }

            sp = sp.replaceAll("\\n{2,}", "\n");

            if (!sp.contains(AnisControl.DC2)) {
                builder.append(sp);
            } else {
                if (sp.contains(AnisControl.DELETE_LINE)) {
                    continue;
                }
                if (sp.contains(AnisControl.ESCAPE)) {
                    builder.append(sp.substring(sp.indexOf(AnisControl.ESCAPE)));
                } else continue;
            }

            if (i != split.length - 1) {
                builder.append(StrUtil.CRLF);
            }
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
