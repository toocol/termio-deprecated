package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.core.term.core.Printer;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;

import static com.toocol.ssh.common.utils.StrUtil.CRLF;
import static com.toocol.ssh.common.utils.StrUtil.SPACE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
record ShellPrinter(Shell shell) {
    
    private static final PrintStream printer = Printer.PRINTER;

    void printErr(String msg) {
        printer.print(msg);
        printer.print(shell.getPrompt());
    }

    boolean printInNormal(String msg) {
        if (msg.contains("export HISTCONTROL=ignoreboth")) {
            return false;
        }
        if (shell.localLastCmd.get().equals(msg)) {
            return false;
        } else if (msg.startsWith("\b\u001B[K")) {
            String[] split = msg.split("\r\n");
            if (split.length == 1) {
                return false;
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
        printer.print(msg);
        return true;
    }

    void printInVim(String msg) {
        if (shell.localLastInput.trim().equals(msg.replaceAll("\r\n", ""))) {
            return;
        }
        printer.print(msg);
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
                printer.print(SPACE);
            }
            return;
        }
        if (msg.equals(shell.localLastInput)) {
            return;
        }

        // remove system prompt voice
        if (msg.contains("\u0007")) {
            msg = msg.replaceAll("\u0007", "");
            if (!msg.contains(CRLF)) {
                printer.print(msg);
                String tmp = msg;
                shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
                shell.localLastCmd.getAndUpdate(prev -> prev.replaceAll("\t", "") + tmp);
                return;
            }
        }
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (!msg.contains(CRLF)) {
            printer.print(msg);
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
                    printer.print("\b");
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
                        shell.remoteCmd.set(input.split("#")[1].trim());
                    }
                    if (shell.tabFeedbackRec.contains(input)) {
                        continue;
                    }
                    printer.print(CRLF + input);
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
        printer.print(msg);
    }
}
