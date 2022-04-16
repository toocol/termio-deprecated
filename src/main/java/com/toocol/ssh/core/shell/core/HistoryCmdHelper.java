package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.core.Printer;

import java.util.Stack;
import java.util.UUID;

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
    /**
     * the flag to record user input command;
     */
    private final String flag = UUID.randomUUID().toString();

    private String upBuffer = null;
    private String downBuffer = null;
    private boolean puToDown = false;
    private boolean start = false;

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
        reset();
    }

    /*
     * when user press the up arrow.
     **/
    public synchronized void up() {
        start = true;
        if (downBuffer != null) {
            downArrowStack.push(downBuffer);
            downBuffer = null;
        }
        if ((upArrowStack.isEmpty() && downArrowStack.isEmpty())
                || (!downArrowStack.isEmpty() && puToDown)) {
            baseCmdStack.forEach(upArrowStack::push);
            puToDown = false;
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
        shell.currentPrint.set(cmd);
        shell.selectHistoryCmd.set(cmd);
        shell.cmd.delete(0, shell.cmd.length());
        shell.cmd.append(cmd);
        if (StrUtil.EMPTY.equals(cmd)) {
            upBuffer = null;
        } else {
            upBuffer = cmd;
        }

        shell.clearShellLineWithPrompt();
        Printer.print(cmd);
    }

    /*
     * when user press the down arrow
     **/
    public synchronized void down() {
        if (upArrowStack.isEmpty()) {
            return;
        }
        if (upBuffer != null) {
            upArrowStack.push(upBuffer);
            upBuffer = null;
        }
        String cmd;
        if (downArrowStack.isEmpty()) {
            start = false;
            cmd = StrUtil.EMPTY;
        } else {
            cmd = downArrowStack.pop();
        }
        if (downBuffer != null) {
            upArrowStack.push(downBuffer);
        }
        boolean resetFlag = false;
        if (cmd.contains("--" + flag)) {
            cmd = cmd.replaceAll("--" + flag, "");
            resetFlag = true;
        }
        shell.currentPrint.set(cmd);
        shell.selectHistoryCmd.set(cmd);
        shell.cmd.delete(0, shell.cmd.length());
        shell.cmd.append(cmd);
        if (StrUtil.EMPTY.equals(cmd)) {
            downBuffer = null;
        } else {
            downBuffer = cmd;
        }
        if (resetFlag) {
            reset();
        }

        shell.clearShellLineWithPrompt();
        Printer.print(cmd);
    }

    public synchronized void pushToDown(String cmd) {
        puToDown = true;
        downArrowStack.push(cmd + "--" + flag);
    }

    public synchronized void reset() {
        upArrowStack.clear();
        downArrowStack.clear();
        upBuffer = null;
        downBuffer = null;
        start = false;
        puToDown = false;
    }

    public boolean isStart() {
        return start;
    }

}
