package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Stack;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 0:26
 * @version: 0.0.1
 */
public class HistoryCmdHelper {

    /**
     * TODO: need accomplish download function first.
     */
    private final Stack<String> historyCmdStack = new Stack<>();
    /**
     * The stack storage all the executed cmd.
     */
    private final Stack<String> baseCmdStack = new Stack<>();
    /**
     * when user press the up arrow, use this stack.
     * clear out when user execute new cmd, which means invoke the method {HistoryCmdHelper.push(String cmd)}
     */
    private final Stack<String> upArrowStack = new Stack<>();
    /**
     * when user press the down arrow, use this stack.
     * clear out when user execute new cmd, which means invoke the method {HistoryCmdHelper.push(String cmd)}
     */
    private final Stack<String> downArrowStack = new Stack<>();

    public synchronized void initialize(String bashHistoryFileData) {
        historyCmdStack.push(bashHistoryFileData);
        historyCmdStack.forEach(baseCmdStack::push);
    }

    public synchronized void push(String cmd) {
        baseCmdStack.push(cmd);
        upArrowStack.clear();
        downArrowStack.clear();
    }

    /*
     * when user press the up arrow.
     **/
    public synchronized String up() {
        baseCmdStack.forEach(upArrowStack::push);
        String cmd = upArrowStack.pop();
        if (StringUtils.isEmpty(cmd)) {
            return StrUtil.EMPTY;
        }
        downArrowStack.push(cmd);
        return cmd;
    }

    /*
     * when user press the down arrow
     **/
    public synchronized String down() {
        String cmd = downArrowStack.pop();
        if (StringUtils.isEmpty(cmd)) {
            return StrUtil.EMPTY;
        }
        upArrowStack.push(cmd);
        return cmd;
    }

}
