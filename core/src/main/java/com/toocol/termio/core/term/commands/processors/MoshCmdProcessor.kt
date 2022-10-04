package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.Tuple2
import org.apache.commons.lang3.StringUtils

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/27 16:19
 */
class MoshCmdProcessor : TermCommandProcessor() {
    private val credentialCache = CredentialCache.Instance

    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        val split = cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), " ").split(" ").toTypedArray()
        if (split.size != 2) {
            resultAndMsg.first(false).second("Wrong mosh cmd, the correct is 'mosh index'")
            return null
        }
        val indexStr = split[1]
        if (!StringUtils.isNumeric(indexStr)) {
            resultAndMsg.first(false).second("The index must be numbers.")
            return null
        }
        val idx: Int = try {
            indexStr.toInt()
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
//        eventBus.send(MoshAddress.ESTABLISH_MOSH_SESSION.address(), idx)
        resultAndMsg.first(true)
        return null
    }
}