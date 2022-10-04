package com.toocol.termio.core.shell.api

import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.file.api.FileApi
import com.toocol.termio.core.shell.core.SftpChannelProvider
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.module.SuspendApi
import com.toocol.termio.utilities.utils.FileNameUtil
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.*
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/4 16:43
 * @version: 0.0.1
 */
object RemoteFileApi : SuspendApi, CoroutineScope by MainScope() {
    private const val dfTypeFile = 1
    private const val dFTypeBytes = 2

    private val sftpChannelProvider = SftpChannelProvider.Instance
    private val shellCache = ShellCache.Instance

    suspend fun dfFile(request: JsonObject) {
        coroutineScope {
            launch(Dispatchers.IO) {
                val sessionId = request.getLong("sessionId")
                val remotePath = request.getString("remotePath")
                val channelSftp = sftpChannelProvider.getChannelSftp(sessionId)
                if (channelSftp == null) {
                    val shell = ShellCache.getShell(sessionId) ?: return@launch
                    shell.printErr("Create sftp channel failed.")
                    return@launch
                }

                val storagePath: String = FileApi.chooseDirectory().orEmpty()
                val shell = ShellCache.getShell(sessionId) ?: return@launch

                Printer.print(shell.getPrompt())
                if (storagePath.isEmpty()) {
                    return@launch
                }
                if (remotePath.contains(",")) {
                    for (rpath in remotePath.split(",").toTypedArray()) {
                        try {
                            channelSftp[rpath, storagePath]
                        } catch (e: Exception) {
                            Printer.println("\ndf: no such file '$rpath'.")
                            Printer.print(shell.getPrompt() + shell.getCurrentPrint())
                        }
                    }
                } else {
                    try {
                        channelSftp[remotePath, storagePath]
                    } catch (e: Exception) {
                        Printer.println("\ndf: no such file '$remotePath'.")
                        Printer.print(shell.getPrompt() + shell.getCurrentPrint())
                    }
                }
            }
        }
    }

    suspend fun dfBytes(request: JsonObject): ByteArray? = withContext(Dispatchers.IO) {
        run {
            try {
                val sessionId = request.getLong("sessionId")
                val remotePath = request.getString("remotePath")
                val type = Optional.ofNullable(request.getInteger("type")).orElse(0)
                if (type != dfTypeFile && type != dFTypeBytes) {
                    return@run null
                }
                val channelSftp = sftpChannelProvider.getChannelSftp(sessionId)
                if (channelSftp == null) {
                    val shell = ShellCache.getShell(sessionId) ?: return@run null
                    shell.printErr("Create sftp channel failed.")
                    return@run null
                }
                val inputStream = channelSftp[remotePath]
                return@run IOUtils.buffer(inputStream).readAllBytes()
            } catch (e: Exception) {
                // Do nothing
                return@run null
            }
        }
    }

    suspend fun uf(request: JsonObject) = withContext(Dispatchers.IO) {
        val sessionId = request.getLong("sessionId")
        val remotePath = request.getString("remotePath")
        val channelSftp = sftpChannelProvider.getChannelSftp(sessionId)
        if (channelSftp == null) {
            val shell = shellCache.getShell(sessionId) ?: return@withContext
            shell.printErr("Create sftp channel failed.")
            return@withContext
        }

        val localPathBuilder = StringBuilder()
        localPathBuilder.append(FileApi.chooseFile().orEmpty())
        val shell = shellCache.getShell(sessionId) ?: return@withContext
        Printer.print(shell.getPrompt())
        val fileNames = localPathBuilder.toString()
        if (fileNames.isEmpty()) {
            return@withContext
        }
        try {
            channelSftp.cd(remotePath)
            for (fileName in fileNames.split(",").toTypedArray()) {
                channelSftp.put(FileInputStream(fileName), FileNameUtil.getName(fileName))
            }
        } catch (e: Exception) {
            // do nothing
        }
    }
}