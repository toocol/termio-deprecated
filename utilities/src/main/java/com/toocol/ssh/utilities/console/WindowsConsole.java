package com.toocol.ssh.utilities.console;

import com.toocol.ssh.utilities.jni.TermioJNI;
import com.toocol.ssh.utilities.utils.AnisControl;
import com.toocol.ssh.utilities.utils.StrUtil;

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
    public String processAnisControl(String msg) {
        if (msg.startsWith("\n\u0004:\u0002@") && msg.length() == 6) {
            return StrUtil.EMPTY;
        }
        if (msg.contains("\u001B[K")) {
            return msg;
        }
        msg = msg.replaceAll("�\\u0001\\u0012�\\u0001\"�\\u0001", StrUtil.EMPTY);
        msg = msg.replaceAll("�\\u0003\\u0012�\\u0003\"�\\u0003", StrUtil.EMPTY);
        msg = msg.replaceAll("�\\u0004\\u0012�\\u0004\"�\\u0004", StrUtil.EMPTY);
        msg = msg.replaceAll("�\b\\u0012�\b\"�\b", StrUtil.EMPTY);
        StringBuilder builder = new StringBuilder();
        String[] split = msg.split(StrUtil.CRLF);
        for (int i = 0; i < split.length; i++) {
            String sp = split[i];
            if (!sp.contains(AnisControl.DEVICE_CONTROL)) {
                builder.append(sp);
            }  else continue;

            if (i != split.length - 1) {
                builder.append(StrUtil.CRLF);
            }
        }
        return builder.toString();
    }
}
