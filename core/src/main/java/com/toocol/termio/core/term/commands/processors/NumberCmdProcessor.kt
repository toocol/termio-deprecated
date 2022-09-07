package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.ssh.SshAddress
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.Tuple2
import io.vertx.core.eventbus.EventBus
import org.apache.commons.lang3.StringUtils

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 17:34
 */
class NumberCmdProcessor : TermCommandProcessor() {
    private val credentialCache = CredentialCache.Instance

    override fun process(eventBus: EventBus, cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        if (!StringUtils.isNumeric(cmd)) {
            resultAndMsg.first(false).second("There can't be any spaces or other character between numbers.")
            return null
        }
        val idx: Int = try {
            cmd.toInt()
        } catch (e: Exception) {
            resultAndMsg.first(false).second("Number is too long.")
            return null
        }
        if (idx <= 0) {
            resultAndMsg.first(false).second("The input number must > 0.")
            return null
        }
        if (idx > credentialCache.credentialsSize()) {
            resultAndMsg.first(false)
                .second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".")
            return null
        }
        eventBus.send(SshAddress.ESTABLISH_SSH_SESSION.address(), idx)
        resultAndMsg.first(true)
        return null
    }
}