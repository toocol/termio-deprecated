package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.jni.TerminatioJNI;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 11:09
 */
public class Term {

    private Term() {
    }

    public static final String PROMPT = "[terminatio] > ";

    private static final Term INSTANCE = new Term();

    private final TermReader termReader = new TermReader();

    public static Term getInstance() {
        return INSTANCE;
    }

    public int getWidth() {
        return TerminatioJNI.getInstance().getWindowWidth();
    }

    public int getHeight() {
        return TerminatioJNI.getInstance().getWindowHeight();
    }

    public TermReader getReader() {
        return termReader;
    }

}
