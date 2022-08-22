package com.toocol.termio.core.file.core

import com.toocol.termio.utilities.console.Console

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 22:07
 * @version: 0.0.1
 */
class DirectoryChooser {
    /**
     * chose the local path
     *
     * @return file paths
     */
    fun showOpenDialog(): String? {
        return console.chooseDirectory()
    }

    companion object {
        private val console = Console.get()
    }
}