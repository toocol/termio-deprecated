package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TermioJNI;
import com.toocol.ssh.common.utils.Tuple2;
import jline.Terminal;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public class Term {

    private Term() {
    }

    public static final String PROMPT = "[termio] > ";

    private static final Term INSTANCE = new Term();
    private static final TermioJNI JNI = TermioJNI.getInstance();

    public static Term getInstance() {
        return INSTANCE;
    }

    private final Terminal terminal;

    private final TermReader termReader;
    {
        termReader  = new TermReader();
        terminal = termReader.getReader().getTerminal();
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

}
