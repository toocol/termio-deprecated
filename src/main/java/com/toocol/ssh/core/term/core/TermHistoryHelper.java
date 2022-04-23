package com.toocol.ssh.core.term.core;

import com.toocol.ssh.common.utils.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Stack;
import java.util.UUID;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/18 23:16
 * @version: 0.0.1
 */
public final class TermHistoryHelper {

    private final Term term;
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

    public TermHistoryHelper(Term term) {
        this.term = term;
    }

    public synchronized void push(String cmd) {
        if (StringUtils.isEmpty(cmd.trim())) {
            return;
        }
        if (baseCmdStack.isEmpty()) {
            baseCmdStack.push(cmd.trim());
        } else if (!baseCmdStack.peek().equals(cmd.trim())) {
            baseCmdStack.push(cmd.trim());
        }
        reset();
    }

    /*
     * when user press the up arrow.
     **/
    public synchronized String up() {
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
        if (upArrowStack.isEmpty()) {
            return null;
        }
        String cmd = upArrowStack.pop();
        if (upArrowStack.isEmpty()) {
            upArrowStack.push(cmd);
            term.clearTermLineWithPrompt();
            Printer.print(HighlightHelper.assembleColorBackground(cmd, Term.theme.executeLineBackgroundColor));
            if (upBuffer != null) {
                downArrowStack.push(upBuffer);
            }
            upBuffer = null;
            return cmd;
        }

        if (upBuffer != null) {
            downArrowStack.push(upBuffer);
        }
        if (StrUtil.EMPTY.equals(cmd)) {
            upBuffer = null;
        } else {
            upBuffer = cmd;
        }

        term.clearTermLineWithPrompt();
        Printer.print(HighlightHelper.assembleColorBackground(cmd, Term.theme.executeLineBackgroundColor));
        return cmd;
    }

    /*
     * when user press the down arrow
     **/
    public synchronized String down() {
        if (upArrowStack.isEmpty()) {
            return null;
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
        if (StrUtil.EMPTY.equals(cmd)) {
            downBuffer = null;
        } else {
            downBuffer = cmd;
        }
        if (resetFlag) {
            reset();
        }

        term.clearTermLineWithPrompt();
        Printer.print(HighlightHelper.assembleColorBackground(cmd, Term.theme.executeLineBackgroundColor));
        return cmd;
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
