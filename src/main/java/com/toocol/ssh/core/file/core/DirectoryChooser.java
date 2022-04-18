package com.toocol.ssh.core.file.core;

import com.toocol.ssh.common.jni.TermioJNI;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 22:07
 * @version: 0.0.1
 */
public class DirectoryChooser {

    private static final TermioJNI TERMIO_JNI = TermioJNI.getInstance();

    /**
     * multiple file paths separate by ','
     *
     * @return file paths
     */
    public String showOpenDialog() {
        return TERMIO_JNI.chooseDirectory();
    }

}
