package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.cache.CredentialCache
import com.toocol.termio.core.ssh.api.SshApi
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.Tuple2
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.util.*

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:45
 * @version: 0.0.1
 */
class ActiveCmdProcessor : TermCommandProcessor() {

    private val credentialCache = CredentialCache.Instance

    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        val idxs: MutableList<Int> = ArrayList()
        val split = cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), " ").split(" ").toTypedArray()
        try {
            if (split.size <= 1) {
                resultAndMsg.first(false).second("No connection properties selected")
                return null
            } else if (split.size == 2) {
                if (split[1].contains("-")) {
                    val nums = split[1].trim { it <= ' ' }.split("-").toTypedArray()
                    if (nums.size > 2) {
                        resultAndMsg.first(false).second("the input format must be num-num")
                        return null
                    } else {
                        for (num in nums) {
                            if (!StringUtils.isNumeric(num)) {
                                resultAndMsg.first(false).second("The input parameters must be numeric.")
                                return null
                            }
                        }
                        val start = nums[0].toInt()
                        val end = nums[1].toInt()
                        if (start > end) {
                            resultAndMsg.first(false).second("The input parameters must be from small to large")
                            return null
                        } else {
                            for (i in start..end) {
                                idxs.add(i)
                            }
                        }
                    }
                } else {
                    if (!StringUtils.isNumeric(split[1])) {
                        resultAndMsg.first(false).second("The input parameters must be numeric.")
                        return null
                    } else {
                        val idx = split[1].toInt()
                        if (idx <= 0) {
                            resultAndMsg.first(false).second("The input number must > 0.")
                            return null
                        }
                        if (idx > credentialCache.credentialsSize()) {
                            resultAndMsg.first(false)
                                .second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".")
                            return null
                        } else {
                            idxs.add(idx)
                        }
                    }
                }
            } else {
                val list = Arrays.stream(split).filter { e: String -> e != "active" }.toList()
                for (s in list) {
                    if (!StringUtils.isNumeric(s)) {
                        resultAndMsg.first(false).second("The input parameters must be numeric.")
                        return null
                    }
                    val idx = s.toInt()
                    if (idx <= 0) {
                        resultAndMsg.first(false).second("The input number must > 0.")
                        return null
                    }
                    if (idx > credentialCache.credentialsSize()) {
                        resultAndMsg.first(false)
                            .second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".")
                        return null
                    } else {
                        idxs.add(idx)
                    }
                }
            }
        } catch (e: Exception) {
            resultAndMsg.first(false).second("the input number is too long")
        }
        launch {
            SshApi.activeSshSession(idxs)
        }
        resultAndMsg.first(true)
        return null
    }
}