package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.Printer;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.common.utils.StrUtil.CRLF;
import static com.toocol.ssh.common.utils.StrUtil.SPACE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
record ShellPrinter(Shell shell) {

    void printErr(String msg) {
        Printer.printErr(msg);
        Printer.print(shell.getPrompt());
    }

    void printInNormal(String msg) {
        if (msg.contains("export HISTCONTROL=ignoreboth")) {
            return;
        }
        if (shell.localLastCmd.get().equals(msg)) {
            return;
        } else if (msg.startsWith("\b\u001B[K")) {
            String[] split = msg.split("\r\n");
            if (split.length == 1) {
                return;
            }
            msg = split[1];
        } else if (msg.startsWith(shell.localLastCmd.get().trim())) {
            // cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(shell.localLastCmd.get().trim().length());
        }
        if (msg.startsWith(CRLF)) {
            msg = msg.replaceFirst("\r\n", "");
        }

        String tmp = msg;
        shell.currentPrint.getAndUpdate(prev -> prev + tmp);
        Printer.print(msg);
    }

    void printInVim(String msg) {
        Printer.print(msg);
    }

    void printInTabAccomplish(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (msg.contains("\u001B")) {
            return;
        }

        msg = msg.replaceAll("\b", "");
        if (msg.trim().equals(shell.localLastCmd.get().replaceAll("\t", ""))) {
            if (msg.endsWith(SPACE)) {
                Printer.print(SPACE);
            }
            return;
        }
        if (msg.equals(shell.localLastInput)) {
            return;
        }

        // remove system prompt voice
        if (msg.contains("\u0007")) {
            msg = msg.replaceAll("\u0007", "");
            Printer.print(msg);
            String tmp = msg;
            shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
            shell.localLastCmd.getAndUpdate(prev -> prev.replaceAll("\t", "") + tmp);
            return;
        }
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (!msg.contains(CRLF)) {
            Printer.print(msg);
            String tmp = msg;
            shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
            shell.localLastCmd.getAndUpdate(prev -> prev.replaceAll("\t", "") + tmp);
            return;
        }

        String[] split = msg.split("\r\n");
        if (split.length != 0) {
            String localLine = shell.getPrompt()
                    + shell.localLastCmd.get()
                    .replaceAll("\t", "")
                    .replaceAll("\b", "")
                    .replaceAll("\u001B", "")
                    .replaceAll("\\[K", "");
            if (!split[split.length - 1].equals(localLine)) {
                // have already auto-accomplish address
                int backspaceLen = (localLine + shell.currentPrint).length();
                for (int idx = 0; idx < backspaceLen; idx++) {
                    Printer.print("\b");
                }
                msg = split[split.length - 1];
                if (msg.split("#").length == 2) {
                    shell.remoteCmd.set(msg.split("#")[1].trim());
                }
            } else {
                for (String input : split) {
                    if (StringUtils.isEmpty(input)) {
                        continue;
                    }
                    if (input.split("#").length == 2) {
                        shell.remoteCmd.set(msg.split("#")[1].trim());
                    }
                    if (shell.tabFeedbackRec.contains(input)) {
                        continue;
                    }
                    Printer.print(CRLF + input);
                    shell.currentPrint.set(input);
                    shell.tabFeedbackRec.add(input);
                }
                return;
            }
        }
        if (msg.startsWith(CRLF)) {
            msg = msg.replaceFirst("\r\n", "");
        }

        String tmp = msg;
        shell.currentPrint.getAndUpdate(prev -> prev + tmp);
        Printer.print(msg);
    }

    void printSelectHistoryCommand(String msg) {
        shell.selectHistoryCmd.set(msg.replaceAll("\b", "").replaceAll("\u001B", "").replaceAll("\\[K", ""));
        Printer.print(msg);
    }
}
