package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TermioJNI;
import com.toocol.ssh.common.utils.Tuple2;
import jline.Terminal;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public class Term {

    public static final String PROMPT = "[termio] > ";

    private static final Term INSTANCE = new Term();
    private static final TermioJNI JNI = TermioJNI.getInstance();

    private final Terminal terminal;
    private final TermReader termReader;

    private Term() {
        termReader  = new TermReader(this);
        terminal = termReader.getReader().getTerminal();
    }


    public static Term getInstance() {
        return INSTANCE;
    }

    public int getWidth() {
        return terminal.getWidth();
    }

    public int getHeight() {
        return terminal.getHeight();
    }

    public TermReader getReader() {
        return termReader;
    }

    public Tuple2<Integer, Integer> getCursorPosition() {
        String[] coord = JNI.getCursorPosition().split(",");
        return new Tuple2<>(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]));
    }

    public void setCursorPosition(int x, int y) {
        JNI.setCursorPosition(x, y);
    }

    public void showCursor() {
        JNI.showCursor();
    }

    public void hideCursor() {
        JNI.hideCursor();
    }

    public void cursorLeft() {
        JNI.cursorLeft();
    }

    public void cursorRight() {
        JNI.cursorRight();
    }

    public void clearShellLineWithPrompt() {
        int promptLen = PROMPT.length();
        Tuple2<Integer, Integer> position = getCursorPosition();
        int cursorX = position._1();
        int cursorY = position._2();
        hideCursor();
        setCursorPosition(promptLen, cursorY);
        for (int idx = 0; idx < cursorX - promptLen; idx++) {
            Printer.print(" ");
        }
        setCursorPosition(promptLen, cursorY);
        showCursor();
    }
}
