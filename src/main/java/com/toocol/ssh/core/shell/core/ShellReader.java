package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.console.ConsoleReader;
import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 2:07
 * @version: 0.0.1
 */
record ShellReader(Shell shell, OutputStream outputStream) {

    private final static ConsoleReader reader = ConsoleReader.get(System.in);

    void readCmd() throws Exception {
        shell.cmd.delete(0, shell.cmd.length());
        StringBuilder localLastInputBuffer = new StringBuilder();
        while (true) {
            char inChar = (char) reader.readVirtualKey();
            if (inChar == '\uFFFF') {
                throw new RuntimeException("Can't handle chinese character.");
            }
            if (shell.status.equals(Shell.Status.VIM_UNDER)) {
                outputStream.write(inChar);
                outputStream.flush();
            } else {
                if (inChar == CharUtil.CTRL_C) {

                    outputStream.write(inChar);
                    outputStream.flush();
                    shell.status = Shell.Status.NORMAL;

                } else if (inChar == CharUtil.UP_ARROW || inChar == CharUtil.DOWN_ARROW) {

                    shell.status = Shell.Status.NORMAL;
                    if (!shell.historyCmdHelper.isStart()) {
                        if (shell.cmd.length() != 0 && StringUtils.isEmpty(shell.remoteCmd.get())) {
                            shell.historyCmdHelper.pushToDown(shell.cmd.toString());
                        } else if (StringUtils.isNotEmpty(shell.remoteCmd.get())) {
                            byte[] wirte = "\u007F".repeat(shell.remoteCmd.get().length()).getBytes(StandardCharsets.UTF_8);
                            if (wirte.length > 0) {
                                outputStream.write(wirte);
                                outputStream.flush();
                                String cmdToPush = shell.remoteCmd.get().replaceAll("\u007F", "");
                                shell.historyCmdHelper.pushToDown(cmdToPush);
                            }
                        }
                    }
                    if (inChar == CharUtil.UP_ARROW) {
                        shell.historyCmdHelper.up();
                    } else {
                        shell.historyCmdHelper.down();
                    }
                    shell.localLastCmd.set("");

                } else if (inChar == CharUtil.TAB) {

                    if (shell.status.equals(Shell.Status.NORMAL)) {
                        shell.localLastCmd.set(shell.cmd.toString());
                        shell.remoteCmd.set(shell.cmd.toString());
                    }
                    shell.localLastInput = localLastInputBuffer.toString();
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
                        shell.remoteCmd.set(StrUtil.EMPTY);
                        shell.localLastCmd.set(StrUtil.EMPTY);
                        shell.selectHistoryCmd.set(StrUtil.EMPTY);
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
                            shell.remoteCmd.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
                        }
                        if (shell.localLastCmd.get().length() > 0) {
                            shell.localLastCmd.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
                        }
                    }
                    if (shell.status.equals(Shell.Status.NORMAL)) {
                        shell.cmd.deleteCharAt(shell.cmd.length() - 1);
                    }
                    if (shell.currentPrint.get().length() > 0) {
                        shell.currentPrint.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
                    }

                    if (localLastInputBuffer.length() > 0) {
                        localLastInputBuffer = new StringBuilder(localLastInputBuffer.substring(0, localLastInputBuffer.length() - 1));
                    }
                    Printer.virtualBackspace();


                } else if (inChar == CharUtil.CR || inChar == CharUtil.LF) {

                    if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        shell.localLastCmd.set(shell.remoteCmd.get() + StrUtil.CRLF);
                    }
                    shell.localLastInput = localLastInputBuffer.toString();
                    shell.lastRemoteCmd = shell.remoteCmd.get();
                    shell.lastExecuteCmd = StringUtils.isEmpty(shell.remoteCmd.get()) ? shell.cmd.toString() : shell.remoteCmd.get().replaceAll("\b", "");
                    if (!StrUtil.EMPTY.equals(shell.lastExecuteCmd)) {
                        shell.historyCmdHelper.push(shell.lastExecuteCmd);
                    }
                    Printer.print(StrUtil.CRLF);
                    shell.status = Shell.Status.NORMAL;
                    break;
                } else if (CharUtil.isAsciiPrintable(inChar)) {

                    if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        shell.remoteCmd.getAndUpdate(prev -> prev + inChar);
                        shell.localLastCmd.getAndUpdate(prev -> prev + inChar);
                    }
                    shell.currentPrint.getAndUpdate(prev -> prev + inChar);
                    shell.cmd.append(inChar);
                    localLastInputBuffer.append(inChar);
                    Printer.print(String.valueOf(inChar));
                }
            }
        }

    }
}
