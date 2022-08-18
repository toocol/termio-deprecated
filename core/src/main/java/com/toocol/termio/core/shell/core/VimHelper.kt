package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.ASCIIStrCache
import java.nio.charset.StandardCharsets

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/19 14:29
 */
class VimHelper {
    fun transferVimInput(inChar: Char): ByteArray {
        return when (inChar) {
            CharUtil.UP_ARROW -> "\u001B[1A".toByteArray(StandardCharsets.UTF_8)
            CharUtil.DOWN_ARROW -> "\u001B[1B".toByteArray(StandardCharsets.UTF_8)
            CharUtil.LEFT_ARROW -> "\u001B[1D".toByteArray(StandardCharsets.UTF_8)
            CharUtil.RIGHT_ARROW -> "\u001B[1C".toByteArray(StandardCharsets.UTF_8)
            else -> ASCIIStrCache.toString(inChar).toByteArray(StandardCharsets.UTF_8)
        }
    }
}