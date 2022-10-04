package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.shell.commands.ShellCommand
import com.toocol.termio.core.term.commands.TermCommand
import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.utilities.utils.Tuple2

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 16:21
 */
class HelpCmdProcessor : TermCommandProcessor() {
    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        resultAndMsg.first(true).second(TermCommand.help() + ShellCommand.help())
        return null
    }
}