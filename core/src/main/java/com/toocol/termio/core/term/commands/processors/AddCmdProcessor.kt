package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.auth.api.AuthApi
import com.toocol.termio.core.auth.core.SshCredential
import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.utils.RegexUtils
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.launch

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:07
 */
class AddCmdProcessor : TermCommandProcessor() {

    private val credentialCache = CredentialCache.Instance

    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        val params =
            cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), " ").replaceFirst("add ".toRegex(), "").split(" ")
                .toTypedArray()
        if (params.size < 2 || params.size > 3) {
            resultAndMsg.first(false)
                .second("Wrong 'add' command, the correct pattern is 'add host@user -c=password [-p=port]'.")
            return null
        }
        val hostUser = params[0].split("@").toTypedArray()
        if (hostUser.size != 2) {
            resultAndMsg.first(false)
                .second("Wrong 'add' command, the correct pattern is 'add host@user -c=password [-p=port]'.")
            return null
        }
        val user = hostUser[0]
        val host = hostUser[1]
        if (!RegexUtils.matchIp(host) && !RegexUtils.matchDomain(host)) {
            resultAndMsg.first(false).second("Wrong host format, just supporting Ip/Domain address.")
            return null
        }
        val passwordParam = params[1].split("=").toTypedArray()
        if (passwordParam.size != 2) {
            resultAndMsg.first(false).second("Wrong host format, just supporting Ip address.")
            return null
        }
        val password = passwordParam[1]
        val port: Int = if (params.size == 3) {
            try {
                val portParam = params[2].split("=").toTypedArray()
                if (portParam.size != 2) {
                    resultAndMsg.first(false).second("Wrong host format, just supporting Ip address.")
                    return null
                }
                portParam[1].toInt()
            } catch (e: Exception) {
                resultAndMsg.first(false).second("Port should be numbers.")
                return null
            }
        } else {
            22
        }
        var jumpServer = false
        for (param in params) {
            if ("-j" == param) {
                jumpServer = true
                break
            }
        }
        val credential =
            SshCredential.builder().host(host).user(user).password(password).port(port).jumpServer(jumpServer).build()
        if (credentialCache.containsCredential(credential)) {
            resultAndMsg.first(false).second("Connection property already exist.")
            return null
        }
        launch {
            AuthApi.addCredential(JsonObject(credential.toMap()))
            clear()
            Term.printScene(false)
            Term.printTermPrompt()
        }
        resultAndMsg.first(true)
        return null
    }
}