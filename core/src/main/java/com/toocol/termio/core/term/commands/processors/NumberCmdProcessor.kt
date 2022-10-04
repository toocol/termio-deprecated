package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.ssh.core.TrySshSessionAsync
import com.toocol.termio.core.ssh.core.TrySshSessionSync
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.SnowflakeGuidGenerator
import com.toocol.termio.utilities.utils.Tuple2
import org.apache.commons.lang3.StringUtils

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 17:34
 */
class NumberCmdProcessor : TermCommandProcessor() {
    private val credentialCache = CredentialCache.Instance
    private val guidGenerator = SnowflakeGuidGenerator.getInstance()

    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
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

        resultAndMsg.first(true)

        val credential = credentialCache.getCredential(idx)
        val sessionId = guidGenerator.nextId()
        credential ?: return null

        TrySshSessionSync(sessionId, credential.host, credential.user, credential.password).dispatch()
        TrySshSessionAsync(sessionId).dispatch()

        return null
    }
}