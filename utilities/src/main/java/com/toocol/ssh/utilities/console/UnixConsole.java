package com.toocol.ssh.utilities.console;

import com.toocol.ssh.utilities.anis.Printer;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/24 11:47
 */
public final class UnixConsole extends Console {

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
        Printer.print("\u001b[" + x + ";" + y + "H");
    }

    @Override
    public void cursorBackLine(int lines) {

    }

    @Override
    public void showCursor() {
        Printer.print("\u001B[?25l");
    }

    @Override
    public void hideCursor() {
        Printer.print("\u001B[?25h");
    }

    @Override
    public void cursorLeft() {
        Printer.print("\u001B[1D");
    }

    @Override
    public void cursorRight() {
        Printer.print("\u001B[1C");
    }

    @Override
    public byte[] cleanUnsupportedCharacter(byte[] bytes) {
        return bytes;
    }
}
