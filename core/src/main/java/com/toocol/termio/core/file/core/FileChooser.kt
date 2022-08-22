package com.toocol.termio.core.file.core

import com.toocol.termio.utilities.console.Console

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 22:07
 * @version: 0.0.1
 */
class FileChooser {
    /**
     * multiple file paths separate by ','
     *
     * @return file paths
     */
    fun showOpenDialog(): String? {
        val files = console.chooseFiles()
        return files?.substring(0, files.length - 1)
    }

    companion object {
        private val console = Console.get()
    }
}