package com.toocol.termio.core.shell.api

import com.toocol.termio.core.cache.ACCESS_EXHIBIT_SHELL_WITH_PROMPT
import com.toocol.termio.core.cache.EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE
import com.toocol.termio.core.cache.JUST_CLOSE_EXHIBIT_SHELL
import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.shell.core.CmdFeedbackHelper
import com.toocol.termio.core.shell.core.ExecChannelProvider
import com.toocol.termio.core.shell.core.ShellProtocol
import com.toocol.termio.utilities.module.SuspendApi
import com.toocol.termio.utilities.sync.SharedCountdownLatch
import com.toocol.termio.utilities.utils.StrUtil
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/4 17:00
 * @version: 0.0.1
 */
object ShellCmdApi : SuspendApi {
    class Lock

    private val shellCache = ShellCache.Instance
    private val execChannelProvider = ExecChannelProvider.Instance

    suspend fun executeCmdInShell(request: JsonObject): String? = withContext(Dispatchers.IO) {
        run {
            val sessionId = request.getLong("sessionId")
            val cmd = request.getString("cmd")
            val prefix = request.getString("prefix")
            SharedCountdownLatch.await(
                {
                    JUST_CLOSE_EXHIBIT_SHELL = true
                    EXHIBIT_WAITING_BEFORE_COMMAND_PREPARE = false
                },
                this.javaClass,
                Lock::class.java
            )
            val shell = shellCache.getShell(sessionId) ?: return@run "/"

            val inputStream = shell.getInputStream(ShellProtocol.SSH)
            val outputStream = shell.getOutputStream(ShellProtocol.SSH) ?: return@run "/"

            outputStream.write((cmd + StrUtil.LF).toByteArray(StandardCharsets.UTF_8))
            outputStream.flush()
            val feedback = CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback()
            ACCESS_EXHIBIT_SHELL_WITH_PROMPT = false
//            eventBus.send(ShellAddress.DISPLAY_SHELL.address(), sessionId)
            return@run feedback
        }
    }

    suspend fun executeSingleCmd(request: JsonObject): String? = withContext(Dispatchers.IO) {
        run {
            val sessionId = request.getLong("sessionId")
            val cmd = request.getString("cmd")
            val prefix = request.getString("prefix")
            val channelExec = execChannelProvider.getChannelExec(sessionId)
            val shell = shellCache.getShell(sessionId)
            if (shell == null) {
                error("ChannelExec or shell is null.")
                return@run null
            }
            val inputStream = channelExec.inputStream
            channelExec.setCommand(cmd)
            channelExec.connect()
            val feedback = CmdFeedbackHelper(inputStream, cmd, shell, prefix).extractFeedback()
            channelExec.disconnect()
            return@run feedback
        }
    }
}