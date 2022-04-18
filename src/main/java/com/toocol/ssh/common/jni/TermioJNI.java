package com.toocol.ssh.common.jni;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 16:23
 */
public class TermioJNI {

    public native String chooseFiles();

    public native String chooseDirectory();

    public native int getWindowWidth();

    public native int getWindowHeight();

    public native String getCursorPosition();

    public native void setCursorPosition(int x, int y);

    public native void cursorBackLine(int lines);

    public native void showCursor();

    public native void hideCursor();

    public native void cursorLeft();

    public native void cursorRight();

    private TermioJNI() {}

    private static final TermioJNI INSTANCE;
    static {
        INSTANCE = new TermioJNI();
    }

    public static TermioJNI getInstance() {
        return INSTANCE;
    }
}
