package com.toocol.ssh.core.file.core;

import com.toocol.ssh.utilities.console.Console;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 22:07
 * @version: 0.0.1
 */
public class FileChooser {

    private static final Console CONSOLE = Console.get();

    /**
     * multiple file paths separate by ','
     *
     * @return file paths
     */
    public String showOpenDialog() {
        String files = CONSOLE.chooseFiles();
        return files == null ? null : files.substring(0, files.length() - 1);
    }

}
