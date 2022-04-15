package com.toocol.ssh.core.file.core;

import com.toocol.ssh.common.jni.TerminatioJNI;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 22:07
 * @version: 0.0.1
 */
public class FileChooser {

    private static final TerminatioJNI terminatioJNI = TerminatioJNI.getInstance();

    /**
     * multiple file paths separate by ','
     *
     * @return file paths
     */
    public String showOpenDialog() {
        String files = terminatioJNI.chooseFiles();
        return files.substring(0, files.length() - 1);
    }

}
