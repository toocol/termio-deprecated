package com.toocol.termio.core.auth.module

import com.toocol.termio.core.auth.core.SecurityCoder
import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.auth.handlers.AddCredentialHandler
import com.toocol.termio.core.auth.handlers.DeleteCredentialHandler
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.cache.CredentialCache.Instance.addCredential
import com.toocol.termio.core.file.FileAddress
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.module.AbstractModule
import com.toocol.termio.utilities.module.ModuleDeployment
import com.toocol.termio.utilities.module.RegisterHandler
import com.toocol.termio.utilities.utils.FileUtil
import com.toocol.termio.utilities.utils.MessageBox
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer
import kotlin.system.exitProcess

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 15:03
 */
@ModuleDeployment
@RegisterHandler(handlers = [
    AddCredentialHandler::class,
    DeleteCredentialHandler::class
])
class AuthModule : AbstractModule() {
    private val credentialCache = CredentialCache
    @Throws(Exception::class)
    override fun start() {
        val filePath = FileUtil.relativeToFixed("./.credentials")
        mountHandler(vertx, context)
        val coder = SecurityCoder.get()
        context.executeBlocking({ promise: Promise<Any?> -> vertx.eventBus()
            .request(FileAddress.CHECK_FILE_EXIST.address(),
                filePath,
                Handler { reply: AsyncResult<Message<Any?>?>? -> promise.complete() })
        }) {
            vertx.eventBus()
                .request(FileAddress.READ_FILE.address(), filePath, Handler { reply: AsyncResult<Message<Any?>> ->
                    var sshCredentialsStr = cast<String>(reply.result().body())
                    if (coder != null) {
                        sshCredentialsStr = coder.decode(sshCredentialsStr)
                        if (sshCredentialsStr == null) {
                            MessageBox.setExitMessage("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                                    ", and try to delete unsafe .credentials at program's home folder.")
                            exitProcess(-1)
                        }
                    }
                    val sshCredentials: JsonArray?
                    try {
                        sshCredentials =
                            if (StringUtils.isEmpty(sshCredentialsStr)) JsonArray() else JsonArray(sshCredentialsStr)
                    } catch (e: Exception) {
                        MessageBox.setExitMessage("Illegal program: the program seems to have been tampered. Please download the official version at https://github.com/Joezeo/termio" +
                                ", and try to delete unsafe .credentials at program's home folder.")
                        exitProcess(-1)
                    }
                    sshCredentials.forEach(Consumer { o: Any? ->
                        val credentialJsonObj = cast<JsonObject>(o)
                        val sshCredential = SshCredential.transFromJson(credentialJsonObj)
                        addCredential(sshCredential)
                    })
                    Printer.LOADING_ACCOMPLISH = true
                })
        }
    }

    @Throws(Exception::class)
    override fun stop() {
    }
}