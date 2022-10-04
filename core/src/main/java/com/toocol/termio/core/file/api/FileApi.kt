package com.toocol.termio.core.file.api

import com.toocol.termio.core.file.core.DirectoryChooser
import com.toocol.termio.core.file.core.FileChooser
import com.toocol.termio.utilities.module.SuspendApi
import com.toocol.termio.utilities.utils.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:33
 * @version: 0.0.1
 */
object FileApi : SuspendApi {
    private val directoryChooser = DirectoryChooser()
    private val fileChooser = FileChooser()

    suspend fun checkFileExist(filePath: String): Boolean = withContext(Dispatchers.IO) {
        FileUtil.checkAndCreateFile(filePath)
    }

    suspend fun chooseDirectory(): String? = withContext(Dispatchers.IO) {
        directoryChooser.showOpenDialog()
    }

    suspend fun chooseFile(): String? = withContext(Dispatchers.IO) {
        fileChooser.showOpenDialog()
    }

    suspend fun readFile(filePath: String): String? = withContext(Dispatchers.IO) {
        File(filePath).run {
            if (!exists()) {
                null
            } else {
                readText()
            }
        }
    }
}