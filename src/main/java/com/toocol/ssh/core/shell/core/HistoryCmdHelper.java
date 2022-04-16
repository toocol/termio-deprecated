package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.core.Printer;

import java.util.Stack;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/15 0:26
 * @version: 0.0.1
 */
public class HistoryCmdHelper {

    private final Shell shell;
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

    private String upBuffer = null;
    private String downBuffer = null;

    public HistoryCmdHelper(Shell shell) {
        this.shell = shell;
    }

    public synchronized void initialize(String[] historyCmds) {
        for (String historyCmd : historyCmds) {
            baseCmdStack.push(historyCmd);
        }
    }

    public synchronized void push(String cmd) {
        baseCmdStack.push(cmd.trim());
        upArrowStack.clear();
        downArrowStack.clear();
        upBuffer = null;
        downBuffer = null;
    }

    /*
     * when user press the up arrow.
     **/
    public synchronized void up() {
        if (downBuffer != null) {
            downArrowStack.push(downBuffer);
            downBuffer = null;
        }
        if (upArrowStack.isEmpty() && downArrowStack.isEmpty()) {
            baseCmdStack.forEach(upArrowStack::push);
        }
        String cmd;
        if (upArrowStack.isEmpty()) {
            cmd = StrUtil.EMPTY;
        } else {
            cmd = upArrowStack.pop();
        }
        if (upBuffer != null) {
            downArrowStack.push(upBuffer);
        }
        shell.clearShellLineWithPrompt();
        shell.currentPrint.set(shell.prompt.get() + cmd);
        shell.selectHistoryCmd.set(cmd);
        shell.cmd.delete(0, shell.cmd.length());
        shell.cmd.append(cmd);
        if (StrUtil.EMPTY.equals(cmd)) {
            upBuffer = null;
        } else {
            upBuffer = cmd;
        }
        Printer.print(cmd);
    }

    /*
     * when user press the down arrow
     **/
    public synchronized void down() {
        if (upBuffer != null) {
            upArrowStack.push(upBuffer);
            upBuffer = null;
        }
        String cmd;
        if (downArrowStack.isEmpty()) {
            cmd = StrUtil.EMPTY;
        } else {
            cmd = downArrowStack.pop();
        }
        if (downBuffer != null) {
            upArrowStack.push(downBuffer);
        }
        shell.clearShellLineWithPrompt();
        shell.currentPrint.set(shell.prompt.get() + cmd);
        shell.selectHistoryCmd.set(cmd);
        shell.cmd.delete(0, shell.cmd.length());
        shell.cmd.append(cmd);
        if (StrUtil.EMPTY.equals(cmd)) {
            downBuffer = null;
        } else {
            downBuffer = cmd;
        }
        Printer.print(cmd);
    }

}
