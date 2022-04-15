package com.toocol.ssh.common.jni;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 16:23
 */
public class TerminatioJNI {

    public native int getCh();

    public native String chooseFiles();

    public native String chooseDirectory();

    public native int getWindowWidth();

    public native int getWindowHeight();

    private TerminatioJNI() {}

    private static final TerminatioJNI INSTANCE;
    static {
        INSTANCE = new TerminatioJNI();
    }

    public static TerminatioJNI getInstance() {
        return INSTANCE;
    }
}
