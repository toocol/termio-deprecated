package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TerminatioJNI;
import com.toocol.ssh.common.utils.Tuple2;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public class Term {

    private Term() {
    }

    public static final String PROMPT = "[terminatio] > ";

    private static final Term INSTANCE = new Term();
    private static final TerminatioJNI JNI = TerminatioJNI.getInstance();

    public static Term getInstance() {
        return INSTANCE;
    }

    private final TermReader termReader = new TermReader();

    public int getWidth() {
        return TerminatioJNI.getInstance().getWindowWidth();
    }

    public int getHeight() {
        return TerminatioJNI.getInstance().getWindowHeight();
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
}
