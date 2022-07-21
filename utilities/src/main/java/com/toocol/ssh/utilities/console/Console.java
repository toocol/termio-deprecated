package com.toocol.ssh.utilities.console;

import com.toocol.ssh.utilities.utils.OsUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/24 11:45
 */
public abstract class Console {

    private static Console console;

    public synchronized static Console get() {
        if (console != null) {
            return console;
        }
        if (OsUtil.isWindows()) {
            console = new WindowsConsole();
        } else {
            console = null;
        }
        return console;
    }

    public abstract String chooseFiles();

    public abstract String chooseDirectory();

    public abstract int getWindowWidth();

    public abstract int getWindowHeight();

    public abstract String getCursorPosition();

    public abstract void setCursorPosition(int x, int y);

    public abstract void cursorBackLine(int lines);

    public abstract void showCursor();

    public abstract void hideCursor();

    public abstract void cursorLeft();

    public abstract void cursorRight();

    public abstract byte[] cleanUnsupportedCharacter(byte[] bytes);

    public abstract void rollingProcessing(String msg);

}
