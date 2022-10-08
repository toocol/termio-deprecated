package com.toocol.termio.core.auth.module

import com.toocol.termio.core.auth.core.SecurityCoder
import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.file.api.FileApi
import com.toocol.termio.utilities.module.ScopeModule
import com.toocol.termio.utilities.utils.Castable
import com.toocol.termio.utilities.utils.FileUtil
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.DelicateCoroutinesApi
import org.apache.commons.lang3.StringUtils
import kotlin.system.exitProcess

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/10/3 23:46
 * @version: 0.0.1
 */
object AuthScopeModule : ScopeModule(), Castable {
    @DelicateCoroutinesApi
    override suspend fun start() {
        val filePath = FileUtil.relativeToFixed("./.credentials")

//        SecurityCoder("SH2DL3SJ-LL5FF4US")
        val coder = SecurityCoder.get()

        FileApi.checkFileExist(filePath)
        FileApi.readFile(filePath)?.run {
            var sshCredentialsStr = cast<String>(this)
            if (coder != null) {
                sshCredentialsStr = coder.decode(sshCredentialsStr)
                if (sshCredentialsStr == null) {
                    val msg = "Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                            ", and try to delete unsafe .credentials at program's home folder."
                    error(msg)
                    MessageBox.setExitMessage(msg)
                    exitProcess(-1)
                }
            }
            val sshCredentials: JsonArray?
            try {
                sshCredentials =
                    if (StringUtils.isEmpty(sshCredentialsStr)) JsonArray() else JsonArray(sshCredentialsStr)
            } catch (e: Exception) {
                val msg = "Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                        ", and try to delete unsafe .credentials at program's home folder."
                error(msg)
                MessageBox.setExitMessage(msg)
                exitProcess(-1)
            }
            sshCredentials.forEach { o: Any? ->
                val credentialJsonObj = cast<JsonObject>(o)
                val sshCredential = SshCredential.transFromJson(credentialJsonObj)
                CredentialCache.addCredential(sshCredential)
            }
            info("Authentication load success.")
        }
    }

    @DelicateCoroutinesApi
    override suspend fun stop() {
    }
}