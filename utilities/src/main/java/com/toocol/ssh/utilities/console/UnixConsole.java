package com.toocol.ssh.utilities.console;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/24 11:47
 */
public final class UnixConsole extends Console{

    @Override
    public String chooseFiles() {
        return null;
    }

    @Override
    public String chooseDirectory() {
        return null;
    }

    @Override
    public int getWindowWidth() {
        return 0;
    }

    @Override
    public int getWindowHeight() {
        return 0;
    }

    @Override
    public String getCursorPosition() {
        return null;
    }

    @Override
    public void setCursorPosition(int x, int y) {

    }

    @Override
    public void cursorBackLine(int lines) {

    }

    @Override
    public void showCursor() {

    }

    @Override
    public void hideCursor() {

    }

    @Override
    public void cursorLeft() {

    }

    @Override
    public void cursorRight() {

    }

    @Override
    public String processAnisControl(String msg) {
        return msg;
    }

    @Override
    public byte[] cleanUnsupportedCharacter(byte[] bytes) {
        return bytes;
    }
}
