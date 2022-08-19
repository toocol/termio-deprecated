package com.toocol.termio.core.term.core

import java.util.Stack
import java.util.UUID
import com.toocol.termio.utilities.utils.StrUtil
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/18 23:16
 * @version: 0.0.1
 */
class TermHistoryCmdHelper {
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
    fun push(cmd: String) {
        if (StringUtils.isEmpty(cmd.trim { it <= ' ' })) {
            return
        }
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
    fun up(): String? {
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
        if (upArrowStack.isEmpty()) {
            return null
        }
        val cmd = upArrowStack.pop()
        if (upArrowStack.isEmpty()) {
            upArrowStack.push(cmd)
            if (upBuffer != null) {
                downArrowStack.push(upBuffer)
            }
            upBuffer = null
            return cmd
        }
        if (upBuffer != null) {
            downArrowStack.push(upBuffer)
        }
        upBuffer = if (StrUtil.EMPTY == cmd) {
            null
        } else {
            cmd
        }
        return cmd
    }

    /*
     * when user press the down arrow
     **/
    @Synchronized
    fun down(): String? {
        if (upArrowStack.isEmpty()) {
            return null
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
        downBuffer = if (StrUtil.EMPTY == cmd) {
            null
        } else {
            cmd
        }
        if (resetFlag) {
            reset()
        }
        return cmd
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