package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Signal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 2:07
 * @version: 0.0.1
 */
record ShellReader(Shell shell, OutputStream outputStream, ConsoleReader reader) {

    void addTrigger() {
        reader.setHistoryEnabled(false);
        /*
         * custom handle CTRL+C
         */
        Signal.handle(new Signal("INT"), signal -> {
            try {
                shell.historyCmdHelper.reset();
                shell.cmd.delete(0, shell.cmd.length());
                outputStream.write(CharUtil.CTRL_C);
                outputStream.flush();
                shell.status = Shell.Status.NORMAL;
            } catch (Exception e) {
                // do nothing
            }
        });
    }

    void readCmd() throws Exception {
        boolean acceptEscape = false;
        boolean acceptBracketsAfterEscape = true;

        shell.cmd.delete(0, shell.cmd.length());
        StringBuilder localLastInputBuffer = new StringBuilder();
        while (true) {
            char inChar = (char) reader.readCharacter();
            if (inChar == '\u001B') {
                acceptEscape = true;
            }
            if (shell.status.equals(Shell.Status.VIM_UNDER)) {
                outputStream.write(inChar);
                outputStream.flush();
            } else if (shell.status.equals(Shell.Status.MORE_PROC)
                    || shell.status.equals(Shell.Status.MORE_EDIT)
                    || shell.status.equals(Shell.Status.MORE_SUB)) {
                boolean support;
                switch (shell.status) {
                    case MORE_PROC -> support = shell.moreHelper.support(inChar);
                    case MORE_SUB -> support = shell.moreHelper.supportSub(inChar);
                    case MORE_EDIT -> support = shell.moreHelper.supportEdit(inChar);
                    default -> support = false;
                }
                if (support) {
                    outputStream.write(inChar);
                    outputStream.flush();
                }
            } else {
                if (inChar == CharUtil.UP_ARROW || inChar == CharUtil.DOWN_ARROW) {

                    shell.status = Shell.Status.NORMAL;

                    if (inChar == CharUtil.UP_ARROW) {
                        if (!shell.historyCmdHelper.isStart()) {
                            if (shell.cmd.length() != 0 && StringUtils.isEmpty(shell.remoteCmd.get())) {
                                shell.historyCmdHelper.pushToDown(shell.cmd.toString());
                            } else if (StringUtils.isNotEmpty(shell.remoteCmd.get())) {
                                byte[] write = "\u007F".repeat(shell.remoteCmd.get().length()).getBytes(StandardCharsets.UTF_8);
                                if (write.length > 0) {
                                    outputStream.write(write);
                                    outputStream.flush();
                                    String cmdToPush = shell.remoteCmd.get().toString().replaceAll("\u007F", "");
                                    shell.historyCmdHelper.pushToDown(cmdToPush);
                                }
                            }
                        }
                        shell.historyCmdHelper.up();
                    } else {
                        shell.historyCmdHelper.down();
                    }
                    shell.localLastCmd.set(new StringBuffer());

                } else if (inChar == CharUtil.LEFT_ARROW || inChar == CharUtil.RIGHT_ARROW) {

                    int cursorX = shell.term.getCursorPosition()._1();
                    if (inChar == CharUtil.LEFT_ARROW) {
                        if (cursorX > shell.prompt.get().length()) {
                            shell.term.cursorLeft();
                        }
                    } else {
                        if (cursorX < (shell.currentPrint.get().length() + shell.prompt.get().length())) {
                            shell.term.cursorRight();
                        }
                    }
                    if (shell.remoteCmd.get().length() > 0) {
                        outputStream.write(inChar);
                        outputStream.flush();
                    }

                } else if (inChar == CharUtil.TAB) {

                    if (shell.status.equals(Shell.Status.NORMAL)) {
                        shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.cmd));
                        shell.remoteCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.cmd));
                    }
                    shell.localLastInput.delete(0, shell.localLastInput.length()).append(localLastInputBuffer);
                    localLastInputBuffer = new StringBuilder();
                    shell.tabFeedbackRec.clear();
                    outputStream.write(shell.cmd.append(CharUtil.TAB).toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    shell.cmd.delete(0, shell.cmd.length());
                    shell.status = Shell.Status.TAB_ACCOMPLISH;

                } else if (inChar == CharUtil.BACKSPACE) {
                    int cursorX = Term.getInstance().getCursorPosition()._1();
                    if (cursorX <= shell.prompt.get().length()) {
                        Printer.voice();
                        shell.remoteCmd.getAndUpdate(prev -> prev.delete(0, prev.length()));
                        shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()));
                        shell.selectHistoryCmd.getAndUpdate(prev -> prev.delete(0, prev.length()));
                        shell.cmd.delete(0, shell.cmd.length());
                        if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                            outputStream.write(3);
                            outputStream.flush();
                        }
                        continue;
                    }

                    if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        // This is ctrl+backspace
                        shell.cmd.append('\u007F');
                        if (shell.remoteCmd.get().length() > 0) {
                            shell.remoteCmd.getAndUpdate(prev -> new StringBuffer(prev.toString().substring(0, prev.length() - 1)));
                        }
                        if (shell.localLastCmd.get().length() > 0) {
                            shell.localLastCmd.getAndUpdate(prev -> new StringBuffer(prev.substring(0, prev.length() - 1)));
                        }
                    }
                    if (shell.status.equals(Shell.Status.NORMAL)) {
                        shell.cmd.deleteCharAt(shell.cmd.length() - 1);
                    }
                    if (shell.currentPrint.get().length() > 0) {
                        shell.currentPrint.getAndUpdate(prev -> new StringBuffer(prev.substring(0, prev.length() - 1)));
                    }

                    if (localLastInputBuffer.length() > 0) {
                        localLastInputBuffer = new StringBuilder(localLastInputBuffer.substring(0, localLastInputBuffer.length() - 1));
                    }
                    Printer.virtualBackspace();


                } else if (inChar == CharUtil.CR || inChar == CharUtil.LF) {

                    if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.remoteCmd.get().toString()).append(StrUtil.CRLF));
                    }
                    shell.localLastInput.delete(0, shell.localLastInput.length()).append(localLastInputBuffer);
                    shell.lastRemoteCmd.delete(0, shell.lastRemoteCmd.length()).append(shell.remoteCmd.get().toString());
                    shell.lastExecuteCmd.delete(0, shell.lastExecuteCmd.length())
                            .append(StringUtils.isEmpty(shell.remoteCmd.get()) ? shell.cmd.toString() : shell.remoteCmd.get().toString().replaceAll("\b", ""));
                    if (!StrUtil.EMPTY.equals(shell.lastExecuteCmd.toString()) && shell.status == Shell.Status.NORMAL) {
                        shell.historyCmdHelper.push(shell.lastExecuteCmd.toString());
                    }
                    Printer.print(StrUtil.CRLF);
                    shell.status = Shell.Status.NORMAL;
                    break;
                } else if (CharUtil.isAsciiPrintable(inChar)) {

                    if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        shell.remoteCmd.getAndUpdate(prev -> prev.append(inChar));
                        shell.localLastCmd.getAndUpdate(prev -> prev.append(inChar));
                    }
                    shell.currentPrint.getAndUpdate(prev -> prev.append(inChar));
                    shell.cmd.append(inChar);
                    localLastInputBuffer.append(inChar);
                    Printer.print(String.valueOf(inChar));
                }
            }
        }

    }

    private void transferCombo() throws IOException {
//        String escape = String.valueOf((char) reader.readCharacter());
//        return switch (escape) {
//            case "A" -> CharUtil.UP_ARROW;
//            case "B" -> CharUtil.DOWN_ARROW;
//            case "C" -> CharUtil.RIGHT_ARROW;
//            case "D" -> CharUtil.LEFT_ARROW;
//            default -> '\0';
//        };
    }
}
