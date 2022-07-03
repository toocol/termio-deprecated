package com.toocol.ssh.utilities.console;

import com.toocol.ssh.utilities.jni.TermioJNI;
import com.toocol.ssh.utilities.utils.AnisControl;
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
    public String processAnisControl(String msg) {
        if ((msg.startsWith("\n\u0004:\u0002@") && msg.length() == 6)
                || (msg.startsWith("\u0004:\u0002@") && msg.length() == 5)) {
            return StrUtil.EMPTY;
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
            if (!sp.contains(AnisControl.DEVICE_CONTROL)) {
                builder.append(sp);
            } else {
                if (sp.contains(AnisControl.ESCAPE)) {
                    builder.append(sp.substring(sp.indexOf(AnisControl.ESCAPE)));
                } else continue;
            }

            if (i != split.length - 1) {
                builder.append(StrUtil.CRLF);
            }
        }
        return builder.toString();
    }

    @Override
    public byte[] cleanUnsupportedCharacter(byte[] bytes) {
        String msg = new String(bytes, StandardCharsets.UTF_8);
        msg = msg.replaceAll("\\u001A\\u0005\\(�\\u000102", AnisControl.DEVICE_CONTROL);
        msg = msg.replaceAll("�\\b\\u0012�\\b\"�\\b", AnisControl.DEVICE_CONTROL);
        msg = msg.replaceAll("\\u0012Z\"X", AnisControl.DEVICE_CONTROL);
        msg = msg.replaceAll(";\\u00129\"7ls\\u001B", AnisControl.DEVICE_CONTROL);
        for (int i = 0; i < 32; i++) {
            String regex = "\\u000" + i + "\\u0012";
            msg = msg.replaceAll(regex, AnisControl.DEVICE_CONTROL);

            regex = "�\\u000" + i + "\\u0012�\\u000" + i + "\"�\\u000" + i;
            msg = msg.replaceAll(regex, AnisControl.DEVICE_CONTROL);
        }
        return msg.getBytes(StandardCharsets.UTF_8);
    }
}
