package com.toocol.termio.core.shell.core

import com.toocol.termio.utilities.ansi.Printer.print
import com.toocol.termio.utilities.utils.StrUtil
import java.util.*
import java.util.function.Consumer

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 0:26
 * @version: 0.0.1
 */
class ShellHistoryCmdHelper(private val shell: Shell) {
    /**
     * The stack storage all the executed cmd.
     */
    private val baseCmdStack = Stack<String>()

    /**
     * when user press the up arrow, use this stack.
     * clear out when user execute new cmd, which means invoke the method {HistoryCmdHelper.push(String cmd)}
     */
    private val upArrowStack = Stack<String>()

    /**
     * when user press the down arrow, use this stack.
     * clear out when user execute new cmd, which means invoke the method {HistoryCmdHelper.push(String cmd)}
     */
    private val downArrowStack = Stack<String>()

    /**
     * the flag to record user input command;
     */
    private val flag = UUID.randomUUID().toString()
    private var upBuffer: String? = null
    private var downBuffer: String? = null
    private var puToDown = false
    var isStart = false
        private set

    @Synchronized
    fun initialize(historyCmds: Array<String>) {
        for (historyCmd in historyCmds) {
            if ("export HISTCONTROL=ignoreboth" == historyCmd) {
                continue
            }
            baseCmdStack.push(historyCmd)
        }
    }

    @Synchronized
    fun push(cmd: String) {
        if (baseCmdStack.isEmpty()) {
            baseCmdStack.push(cmd.trim { it <= ' ' })
        } else if (baseCmdStack.peek() != cmd.trim { it <= ' ' }) {
            baseCmdStack.push(cmd.trim { it <= ' ' })
        }
        reset()
    }

    /*
     * when user press the up arrow.
     **/
    @Synchronized
    fun up() {
        isStart = true
        if (downBuffer != null) {
            downArrowStack.push(downBuffer)
            downBuffer = null
        }
        if (upArrowStack.isEmpty() && downArrowStack.isEmpty()
            || !downArrowStack.isEmpty() && puToDown
        ) {
            baseCmdStack.forEach(Consumer { item: String -> upArrowStack.push(item) })
            puToDown = false
        }
        val cmd = upArrowStack.pop()
        if (upArrowStack.isEmpty()) {
            upArrowStack.push(cmd)
            if (upBuffer != null) {
                downArrowStack.push(upBuffer)
            }
            upBuffer = null
            shell.currentPrint.delete(0, shell.currentPrint.length).append(cmd)
            shell.selectHistoryCmd.delete(0, shell.selectHistoryCmd.length).append(cmd)
            shell.cmd.delete(0, shell.cmd.length).append(cmd)
            shell.clearShellLineWithPrompt()
            print(cmd)
            return
        }
        if (upBuffer != null) {
            downArrowStack.push(upBuffer)
        }
        shell.currentPrint.delete(0, shell.currentPrint.length).append(cmd)
        shell.selectHistoryCmd.delete(0, shell.selectHistoryCmd.length).append(cmd)
        shell.cmd.delete(0, shell.cmd.length).append(cmd)
        upBuffer = if (StrUtil.EMPTY == cmd) {
            null
        } else {
            cmd
        }
        shell.clearShellLineWithPrompt()
        print(cmd)
    }

    /*
     * when user press the down arrow
     **/
    @Synchronized
    fun down() {
        if (upArrowStack.isEmpty()) {
            return
        }
        if (upBuffer != null) {
            upArrowStack.push(upBuffer)
            upBuffer = null
        }
        var cmd: String
        if (downArrowStack.isEmpty()) {
            isStart = false
            cmd = StrUtil.EMPTY
        } else {
            cmd = downArrowStack.pop()
        }
        if (downBuffer != null) {
            upArrowStack.push(downBuffer)
        }
        var resetFlag = false
        if (cmd.contains("--$flag")) {
            cmd = cmd.replace("--$flag".toRegex(), "")
            resetFlag = true
        }
        val tmp = cmd
        shell.currentPrint.delete(0, shell.currentPrint.length).append(tmp)
        shell.selectHistoryCmd.delete(0, shell.selectHistoryCmd.length).append(tmp)
        shell.cmd.delete(0, shell.cmd.length).append(cmd)
        downBuffer = if (StrUtil.EMPTY == cmd) {
            null
        } else {
            cmd
        }
        if (resetFlag) {
            reset()
        }
        shell.clearShellLineWithPrompt()
        print(cmd)
    }

    @Synchronized
    fun pushToDown(cmd: String) {
        puToDown = true
        downArrowStack.push("$cmd--$flag")
    }

    @Synchronized
    fun reset() {
        upArrowStack.clear()
        downArrowStack.clear()
        upBuffer = null
        downBuffer = null
        isStart = false
        puToDown = false
    }
}