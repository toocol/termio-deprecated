package com.toocol.termio.core.auth.handlers

import com.toocol.termio.core.auth.AuthAddress
import com.toocol.termio.core.auth.core.SecurityCoder
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.cache.ShellCache.Instance.stop
import com.toocol.termio.core.cache.SshSessionCache.Instance.containSession
import com.toocol.termio.utilities.module.IAddress
import com.toocol.termio.utilities.module.NonBlockingMessageHandler
import com.toocol.termio.utilities.utils.FileUtil
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.Message
import org.apache.commons.lang3.StringUtils
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:18
 */
class DeleteCredentialHandler(vertx: Vertx?, context: Context?) : NonBlockingMessageHandler(
    vertx!!, context!!) {
    private val credentialCache = CredentialCache.Instance
    override fun consume(): IAddress {
        return AuthAddress.DELETE_CREDENTIAL
    }

    override fun <T> handleInline(message: Message<T>) {
        val index = cast<Int>(message.body())
        val host = credentialCache.deleteCredential(index)
        if (StringUtils.isNotEmpty(host)) {
            val sessionId = containSession(host!!)
            stop(sessionId)
        }
        val filePath = FileUtil.relativeToFixed("./.credentials")
        var credentialsJson = credentialCache.credentialsJson
        val coder = SecurityCoder.get()
        if (coder != null) {
            credentialsJson = coder.encode(credentialsJson)
            if (credentialsJson.isNullOrEmpty()) {
                MessageBox.setExitMessage("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                        ", and try to delete unsafe .credentials at program's home folder.")
                exitProcess(-1)
            }
        }
        vertx.fileSystem().writeFile(filePath, Buffer.buffer(credentialsJson)) { }
        message.reply(null)
    }
}