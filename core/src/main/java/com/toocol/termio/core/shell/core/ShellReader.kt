package com.toocol.termio.core.shell.core

import com.toocol.termio.core.term.core.Term
import com.toocol.termio.core.term.core.TermStatus
import com.toocol.termio.utilities.utils.CharUtil
import jline.console.ConsoleReader
import sun.misc.Signal

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 2:07
 * @version: 0.0.1
 */
class ShellReader(private val shell: Shell, private val reader: ConsoleReader?) {
    fun initReader() {
        /*
         * custom handle CTRL+C
         */
        Signal.handle(Signal("INT")) { signal: Signal? ->
            if (Term.status == TermStatus.TERMIO) {
                return@handle
            }
            if (shell.status == Shell.Status.QUICK_SWITCH) {
                return@handle
            }
            try {
                shell.historyCmdHelper.reset()
                shell.localLastCmd.delete(0, shell.localLastCmd.length)
                shell.cmd.delete(0, shell.cmd.length)
                shell.writeAndFlush(CharUtil.CTRL_C)
                shell.status = Shell.Status.NORMAL
            } catch (e: Exception) {
                // do nothing
            }
        }
    }

    @Throws(Exception::class)
    fun readCmd() {
        shell.cmd.delete(0, shell.cmd.length)
        if (reader == null) {
            return
        }
        while (true) {
            val inChar: Char = reader.readCharacter().toChar()

            /*
             * Start to deal with arrow key.
             */
            val finalChar: Char = if (shell.status == Shell.Status.QUICK_SWITCH) {
                shell.escapeHelper.processArrowBundle(inChar, shell, reader)
            } else {
                shell.escapeHelper.processArrowStream(inChar)
            }
            if (shell.status == Shell.Status.VIM_UNDER) {
                val vimChar: Char = shell.escapeHelper.processArrowBundle(finalChar, shell, reader)
                shell.writeAndFlush(shell.vimHelper.transferVimInput(vimChar))
            } else if (shell.status == Shell.Status.MORE_PROC || shell.status == Shell.Status.MORE_EDIT || shell.status == Shell.Status.MORE_SUB) {
                val support: Boolean = when (shell.status) {
                    Shell.Status.MORE_PROC -> shell.moreHelper.support(finalChar)
                    Shell.Status.MORE_SUB -> shell.moreHelper.supportSub(finalChar)
                    Shell.Status.MORE_EDIT -> shell.moreHelper.supportEdit(finalChar)
                    else -> false
                }
                if (support) {
                    shell.writeAndFlush(finalChar)
                }
            } else {
                if (shell.shellCharEventDispatcher.dispatch(shell, finalChar)) {
                    break
                }
            }
        }
    }
}