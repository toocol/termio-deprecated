package com.toocol.termio.desktop.ui.terminal;

import com.toocol.termio.utilities.console.Console;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/8/2 0:43
 * @version: 0.0.1
 */
public final class DesktopConsole extends Console {
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
        return "0,0";
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
    public byte[] cleanUnsupportedCharacter(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public void rollingProcessing(String msg) {

    }
}
