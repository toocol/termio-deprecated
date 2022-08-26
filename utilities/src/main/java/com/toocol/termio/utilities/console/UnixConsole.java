package com.toocol.termio.utilities.console;

import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.OsUtil;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String getCursorPosition() {
        return "0,0";
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

    @NotNull
    @Override
    public byte[] cleanUnsupportedCharacter(@NotNull byte[] bytes) {
        return bytes;
    }

    @Override
    public void rollingProcessing(@NotNull String msg) {

    }

    @Override
    public void clear() {
        try {
            new ProcessBuilder(OsUtil.getExecution(), OsUtil.getExecuteMode(), OsUtil.getClearCmd())
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            // do nothing
        }
    }
}
