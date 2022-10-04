package com.toocol.termio.core.term.commands.processors

import com.toocol.termio.core.term.commands.TermCommandProcessor
import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermTheme.Companion.listTheme
import com.toocol.termio.core.term.core.TermTheme.Companion.nameOf
import com.toocol.termio.utilities.ansi.Printer.clear
import com.toocol.termio.utilities.utils.Tuple2

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:16
 * @version: 0.0.1
 */
class ThemeCmdProcessor : TermCommandProcessor() {
    override fun process(cmd: String, resultAndMsg: Tuple2<Boolean, String?>): Any? {
        val split = cmd.trim { it <= ' ' }.replace(" {2,}".toRegex(), " ").split(" ").toTypedArray()
        if (split.size != 2) {
            resultAndMsg.first(false)
                .second(
                    """Please select the theme, alternative themes:
                    ${listTheme()}""".trimIndent()
                )
            return null
        }
        val theme = split[1]
        val termTheme = nameOf(theme)
        if (termTheme == null) {
            resultAndMsg.first(false)
                .second(
                    """$theme: theme not found.  alternative themes:
                    ${listTheme()}""".trimIndent()
                )
            return null
        }
        Term.theme = termTheme
        clear()
        Term.printScene(false)
        Term.printTermPrompt()
        resultAndMsg.first(true)
        return null
    }
}