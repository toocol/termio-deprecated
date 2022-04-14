package com.toocol.ssh.common.jni;

import com.toocol.ssh.common.utils.FileUtil;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/14 16:23
 */
public class TerminatioJNI {

    public native int getCh();

    public native String chooseFiles();

    public native int getWindowWidth();

    public native int getWindowHeight();

    private TerminatioJNI() {}

    public static final TerminatioJNI INSTANCE;
    static {
        System.load(FileUtil.relativeToFixed("./starter/libs/native/libterminatio.dll"));
        INSTANCE = new TerminatioJNI();
    }

    public static TerminatioJNI getInstance() {
        return INSTANCE;
    }
}
