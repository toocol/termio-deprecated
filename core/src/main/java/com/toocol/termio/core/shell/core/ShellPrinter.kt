package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.ansi.AsciiControl
import com.toocol.termio.utilities.ansi.Printer
import com.toocol.termio.utilities.utils.CharUtil
import com.toocol.termio.utilities.utils.StrUtil
import org.apache.commons.lang3.StringUtils

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
class ShellPrinter(private val shell: Shell) {

    companion object {
        val PROMPT_ECHO_REGEX = Regex(pattern = """(\[(\w*?)@(.*?)][$#]) .*""")
    }

    fun printErr(msg: String?) {
        Printer.print(msg)
        Printer.print(shell.getPrompt())
    }

    fun printInNormal(msgConst: String): Boolean {
        var msg = msgConst
        if (msg.contains("export HISTCONTROL=ignoreboth")) {
            return false
        }
        val splitChar = if (shell.protocol == ShellProtocol.SSH) StrUtil.CRLF else StrUtil.LF
        val lastCmd: String = shell.localLastCmd.toString().trim { it <= ' ' }
        if (lastCmd == "top") {
            Printer.print(msg)
            return true
        }
        //        Shell.CONSOLE.rollingProcessing(msg);
        if (StringUtils.isEmpty(lastCmd) && clean(msg).startsWith(AsciiControl.LF)) {
            msg = msg.replaceFirst(AsciiControl.LF.toRegex(), "")
        }
        if (shell.localLastCmd.toString().trim { it <= ' ' } == msg.trim { it <= ' ' }) {
            return false
        } else if (msg.startsWith(lastCmd) && StringUtils.isNotEmpty(lastCmd)) {
            // SSH: cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(lastCmd.length)
        }
        if (msg.startsWith(splitChar)) {
            msg = msg.replaceFirst(splitChar.toRegex(), "")
        }
        if (shell.protocol == ShellProtocol.MOSH) {
            val sb = StringBuilder()
            for (str in msg.split(splitChar).toTypedArray()) {
                if (str == lastCmd && StringUtils.isNotEmpty(lastCmd) || str.contains(AsciiControl.BEL)) {
                    continue
                }
                sb.append(shell.fillPrompt(str)).append(StrUtil.LF)
            }
            if (sb.isNotEmpty() && sb.toString().contains(shell.getPrompt())) {
                sb.deleteCharAt(sb.length - 1)
            }
            if (!msg.endsWith(AsciiControl.LF) && sb[sb.length - 1] == CharUtil.LF) {
                sb.deleteCharAt(sb.length - 1)
            }
            msg = sb.toString()
        }
//        if (lastCmd == "clear" && msg.contains("\u001B")) {
//            msg = StrUtil.EMPTY
//        }
        val tmp = msg
        if (tmp.contains(shell.prompt.get())) {
            if (tmp.contains(splitChar)) {
                val split = tmp.split(splitChar).toTypedArray()
                for (i in split.indices) {
                    val matcher = PROMPT_ECHO_REGEX.find(split[i])
                    if (matcher != null && i == split.size - 1) {
                        val promptAndEcho = matcher.value
                        val splitPrompt = promptAndEcho.split("# ").toTypedArray()
                        if (splitPrompt.size == 1) {
                            shell.currentPrint.delete(0, shell.currentPrint.length)
                        } else if (splitPrompt.size > 1) {
                            val currentPrint: StringBuffer = shell.currentPrint.delete(0, shell.currentPrint.length)
                            if (splitPrompt[1].startsWith(lastCmd)) {
                                splitPrompt[1] = splitPrompt[1].replaceFirst(lastCmd.toRegex(), "")
                            }
                            val clean = clean(splitPrompt[1])
                            if ("^C" != clean && lastCmd != clean && !shell.prompt.get().startsWith(clean)) {
                                currentPrint.append(clean)
                            }
                        }
                    } else if (matcher != null) {
                        shell.currentPrint.delete(0, shell.currentPrint.length)
                    }
                }
            } else {
                val matcher = PROMPT_ECHO_REGEX.find(msg)
                if (matcher != null) {
                    val promptAndEcho = matcher.value
                    val split = promptAndEcho.split("# ").toTypedArray()
                    if (split.size == 1) {
                        shell.currentPrint.delete(0, shell.currentPrint.length)
                    } else if (split.size > 1) {
                        val currentPrint: StringBuffer = shell.currentPrint.delete(0, shell.currentPrint.length)
                        val clean = clean(split[1])
                        if ("^C" != clean && !lastCmd.startsWith("cd")) {
                            currentPrint.append(clean)
                        }
                    }
                }
                val cursorPosition: IntArray = shell.getCursorPosition()
                if (cursorPosition[0] != 0) {
                    shell.setCursorPosition(0, cursorPosition[1])
                }
            }
        }
        if (msg.contains(splitChar)) {
            val split = msg.split(splitChar).toTypedArray()
            shell.bottomLinePrint = split[split.size - 1]
        } else {
            shell.bottomLinePrint = msg
        }
        Printer.print(msg)
        if (shell.localLastCmd.toString() == Shell.RESIZE_COMMAND) {
            Printer.print(shell.currentPrint.toString())
        }
        return true
    }

    fun printInVim(msg: String) {
        if (shell.localLastInput.toString().trim { it <= ' ' } == msg.replace("\r\n".toRegex(), "")) {
            return
        }
        if (shell.selectHistoryCmd.toString().trim { it <= ' ' } == msg.replace("\r\n".toRegex(), "")) {
            return
        }
        if (msg.contains(StrUtil.CRLF)) {
            val split = msg.split(StrUtil.CRLF).toTypedArray()
            shell.bottomLinePrint = split[split.size - 1]
        } else {
            shell.bottomLinePrint = msg
        }
        Printer.print(msg)
    }

    fun printInTabAccomplish(msgConst: String) {
        var msg = msgConst
        val bel = msg.contains(AsciiControl.BEL)
        msg = msg.replace(AsciiControl.BEL.toRegex(), StrUtil.EMPTY)
        msg = msg.replace(AsciiControl.BS.toRegex(), StrUtil.EMPTY)
        if (shell.protocol == ShellProtocol.MOSH) {
            msg = msg.replace("25l".toRegex(), StrUtil.EMPTY)
        }
        if (StringUtils.isEmpty(msg)) {
            return
        }
        if (msg.contains(AsciiControl.ESCAPE) && shell.protocol == ShellProtocol.SSH) {
            if (bel) Printer.bel()
            return
        }
        if (msg == shell.tabAccomplishLastStroke) {
            if (bel) Printer.bel()
            return
        }
        val splitChar = if (shell.protocol == ShellProtocol.SSH) StrUtil.CRLF else StrUtil.LF
        if (StringUtils.isNotEmpty(shell.currentPrint)
            && msg.contains(shell.currentPrint)
            && msg != shell.currentPrint.toString()
            && !msg.contains(splitChar)
        ) {
            shell.remoteCmd.delete(0, shell.remoteCmd.length).append(clean(msg))
            shell.localLastCmd.delete(0, shell.localLastCmd.length).append(clean(msg))
            shell.currentPrint.delete(0, shell.currentPrint.length).append(clean(msg))
            if (!msg.contains("]# ")) {
                shell.clearShellLineWithPrompt()
            }
            if (bel) Printer.bel()
            Printer.print(msg)
            return
        } else if (StringUtils.isNotEmpty(shell.localLastInput.toString())
            && msg.startsWith(shell.localLastInput.toString())
            && msg != shell.localLastInput.toString()
            && !msg.contains(splitChar)
        ) {
            val tmp: String = msg.substring(shell.localLastInput.length)
            shell.remoteCmd.append(clean(tmp))
            shell.localLastCmd.append(clean(tmp))
            shell.currentPrint.append(clean(tmp))
            if (bel) Printer.bel()
            Printer.print(tmp)
            return
        } else {
            if (msg.trim { it <= ' ' } == shell.localLastCmd.toString().replace("\t".toRegex(), "")) {
                if (msg.endsWith(StrUtil.SPACE)) {
                    if (bel) Printer.bel()
                    Printer.print(StrUtil.SPACE)
                }
                return
            }
            if (msg == shell.localLastInput.toString()) {
                return
            }

            // remove system prompt voice
            if (msg.contains(AsciiControl.BEL)) {
                val split = msg.split(AsciiControl.BEL).toTypedArray()
                if (split.size == 1) {
                    if (split[0] == shell.localLastInput.toString()) {
                        return
                    }
                } else if (split.size == 2) {
                    msg = split[1]
                    if (!msg.contains(splitChar)) {
                        Printer.print(msg)
                        shell.remoteCmd.append(msg)
                        val newVal: String = shell.localLastCmd.toString().replace("\t".toRegex(), "") + msg
                        shell.localLastCmd.delete(0, shell.localLastCmd.length).append(clean(newVal))
                        shell.currentPrint.append(clean(msg))
                        return
                    }
                } else {
                    return
                }
            }
            if (StringUtils.isEmpty(msg)) {
                return
            }
            if (!msg.contains(splitChar)) {
                if (bel) Printer.bel()
                Printer.print(msg)
                shell.remoteCmd.append(clean(msg))
                val newVal: String = shell.localLastCmd.toString().replace("\t".toRegex(), "") + msg
                shell.localLastCmd.delete(0, shell.localLastCmd.length).append(clean(newVal))
                shell.currentPrint.append(clean(msg))
                return
            }
            val split = msg.split(splitChar).toTypedArray()
            if (split.isNotEmpty()) {
                shell.bottomLinePrint = split[split.size - 1]
                for (input in split) {
                    if (StringUtils.isEmpty(input)) {
                        continue
                    }
                    if (input.split("#").toTypedArray().size == 2) {
                        shell.remoteCmd.delete(0, shell.remoteCmd.length)
                            .append(clean(input.split("#").toTypedArray()[1].trim { it <= ' ' }))
                        shell.localLastCmd.delete(0, shell.localLastCmd.length)
                            .append(clean(msg.split("#").toTypedArray()[1].trim { it <= ' ' }))
                    }
                    if (shell.tabFeedbackRec.contains(input)) {
                        continue
                    }
                    Printer.print(StrUtil.CRLF + input)
                    shell.tabFeedbackRec.add(clean(input))
                }
                return
            }
            if (msg.startsWith(splitChar)) {
                msg = msg.replaceFirst(splitChar.toRegex(), "")
            }
        }
        shell.bottomLinePrint = msg
        shell.currentPrint.append(clean(msg))
        if (bel) Printer.bel()
        Printer.print(msg)
    }

    fun printInMore(msg: String) {
        val splitChar = if (shell.protocol == ShellProtocol.SSH) StrUtil.CRLF else StrUtil.LF
        if (shell.localLastInput.toString().trim { it <= ' ' } == msg.replace(splitChar.toRegex(), "")) {
            return
        }
        if (shell.selectHistoryCmd.toString().trim { it <= ' ' } == msg.replace(splitChar.toRegex(), "")) {
            return
        }
        if (msg.contains(splitChar)) {
            val split = msg.split(splitChar).toTypedArray()
            shell.bottomLinePrint = split[split.size - 1]
        } else {
            shell.bottomLinePrint = msg
        }
        Printer.print(msg)
    }

    private fun clean(str: String): String {
        return AsciiControl.clean(str)
    }

}