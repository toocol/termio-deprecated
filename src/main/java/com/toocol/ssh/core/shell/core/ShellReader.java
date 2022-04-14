package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.common.utils.StrUtil;
import jline.ConsoleReader;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 2:07
 * @version: 0.0.1
 */
record ShellReader(Shell shell, OutputStream outputStream) {

    private static ConsoleReader reader = null;

    static {
        try {
            reader = new ConsoleReader(System.in, null);
        } catch (IOException e) {
            Printer.println("Register console reader failed.");
            System.exit(-1);
        }
    }

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

                    shell.status = Shell.Status.HISTORY_COMMAND_SELECT;
                    shell.localLastCmd.set("");
                    outputStream.write(inChar);
                    outputStream.flush();

                } else if (inChar == CharUtil.TAB) {

                    if (shell.status.equals(Shell.Status.NORMAL)) {
                        shell.localLastCmd.set(shell.cmd.toString());
                        shell.remoteCmd.set(shell.cmd.toString());
                    }
                    shell.localLastInput = localLastInputBuffer.toString();
                    localLastInputBuffer = new StringBuilder();
                    shell.cmd.append(inChar);
                    shell.tabFeedbackRec.clear();
                    outputStream.write(shell.cmd.append(CharUtil.TAB).toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    shell.cmd.delete(0, shell.cmd.length());
                    shell.status = Shell.Status.TAB_ACCOMPLISH;

                } else if (inChar == CharUtil.BACKSPACE) {

                    if (shell.cmd.toString().trim().length() == 0 && shell.status.equals(Shell.Status.NORMAL)) {
                        continue;
                    }
                    if (shell.remoteCmd.get().length() == 0 && shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        continue;
                    }
                    if (shell.selectHistoryCmd.get().length() == 0 && shell.status.equals(Shell.Status.HISTORY_COMMAND_SELECT)) {
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
                    if (shell.status.equals(Shell.Status.HISTORY_COMMAND_SELECT)) {
                        shell.cmd.append('\u007F');
                        shell.selectHistoryCmd.getAndUpdate(prev -> prev.substring(0, prev.length() - 1));
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
