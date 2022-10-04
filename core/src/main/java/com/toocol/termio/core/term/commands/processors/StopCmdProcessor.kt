package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.Tuple2

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:27
 */
class StopCmdProcessor : TermCommandProcessor() {
    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        return null
    }
}