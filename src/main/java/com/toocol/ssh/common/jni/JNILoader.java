package com.toocol.ssh.common.jni;

import com.toocol.ssh.common.utils.FileUtil;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/14 23:37
 * @version: 0.0.1
 */
public class JNILoader {
    public static void load() {
        System.load(FileUtil.relativeToFixed("./starter/libs/libterminatio.dll"));
    }
}
