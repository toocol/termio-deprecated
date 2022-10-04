package com.toocol.termio.core.auth.api

import com.toocol.termio.core.auth.core.SecurityCoder
import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.cache.ShellCache
import com.toocol.termio.core.cache.SshSessionCache
import com.toocol.termio.utilities.module.SuspendApi
import com.toocol.termio.utilities.utils.FileUtil
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import java.io.File
import kotlin.system.exitProcess

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/4 0:02
 * @version: 0.0.1
 */
object AuthApi : SuspendApi {
    private val credentialCache = CredentialCache.Instance

    suspend fun addCredential(credentialJson: JsonObject) = withContext(Dispatchers.IO){
        val credential = SshCredential.transFromJson(credentialJson)
        credentialCache.addCredential(credential)
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
        File(filePath).writeText(credentialsJson)
    }

    suspend fun deleteCredential(index: Int) = withContext(Dispatchers.IO) {
        val host = credentialCache.deleteCredential(index)
        if (StringUtils.isNotEmpty(host)) {
            val sessionId = SshSessionCache.containSession(host!!)
            ShellCache.stop(sessionId)
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
        File(filePath).writeText(credentialsJson)
    }
}