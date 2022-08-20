package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.ansi.AsciiControl
import com.toocol.termio.utilities.ansi.AsciiControl.clean
import com.toocol.termio.utilities.utils.StrUtil
import java.io.InputStream

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/11 15:14
 */
class CmdFeedbackHelper(val inputStream: InputStream?, val cmd: String, val shell: Shell, private val prefix: String) {
    @Throws(Exception::class)
    fun extractFeedback(): String? {
        var feedback: String? = null
        val startTime = System.currentTimeMillis()
        val tmp = ByteArray(1024)
        do {
            if (System.currentTimeMillis() - startTime >= 10000) {
                feedback = StrUtil.EMPTY
            }
            inputStream ?: break
            while (inputStream.available() > 0) {
                val i: Int = inputStream.read(tmp, 0, 1024)
                if (i < 0) {
                    break
                }
                val msg = String(tmp, 0, i)
                val matcher: MatchResult? = Shell.PROMPT_PATTERN.find(msg)
                val cleanedMsg =
                    msg.replace(StrUtil.CR.toRegex(), StrUtil.EMPTY).replace(StrUtil.LF.toRegex(), StrUtil.EMPTY)
                        .replace(StrUtil.SPACE.toRegex(), StrUtil.EMPTY)
                if (matcher == null
                    && !msg.contains(StrUtil.CRLF)
                    && cleanedMsg != cmd.replace(StrUtil.SPACE.toRegex(), StrUtil.EMPTY)
                    && cleanedMsg != shell.getLastRemoteCmd().replace(StrUtil.CR.toRegex(), StrUtil.EMPTY)
                        .replace(StrUtil.LF.toRegex(), StrUtil.EMPTY).replace(StrUtil.SPACE.toRegex(), StrUtil.EMPTY)
                    && cleanedMsg != shell.localLastCmd.toString().replace(StrUtil.CR.toRegex(), StrUtil.EMPTY)
                        .replace(StrUtil.LF.toRegex(), StrUtil.EMPTY).replace(StrUtil.SPACE.toRegex(), StrUtil.EMPTY)
                    && !cleanedMsg.contains(AsciiControl.ESCAPE)
                ) {
                    feedback = msg
                } else if (matcher != null) {
                    shell.setPrompt(clean(matcher.value + StrUtil.SPACE))
                    shell.extractUserFromPrompt()
                }
                if (msg.contains(StrUtil.CRLF) || msg.contains(StrUtil.LF)) {
                    val splitCh = if (msg.contains(StrUtil.CRLF)) StrUtil.CRLF else StrUtil.LF
                    for (split in msg.split(splitCh).toTypedArray()) {
                        if (split.startsWith(prefix)) {
                            feedback = split
                        }
                    }
                }
            }
        } while (feedback == null)
        return feedback
    }
}