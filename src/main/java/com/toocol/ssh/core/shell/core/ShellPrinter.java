package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.core.Printer;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toocol.ssh.common.utils.StrUtil.CRLF;
import static com.toocol.ssh.common.utils.StrUtil.SPACE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
record ShellPrinter(Shell shell) {

    private static final PrintStream printer = Printer.PRINTER;
    public static final Pattern PROMPT_ECHO_PATTERN = Pattern.compile("(\\[(\\w*?)@(.*?)]#) .*");

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
        if (tmp.contains(shell.prompt.get())) {

            Matcher matcher = PROMPT_ECHO_PATTERN.matcher(msg);
            if (matcher.find()) {
                String promptAndEcho = matcher.group(0);
                String[] split = promptAndEcho.split("# ");
                if (split.length == 1) {
                    shell.currentPrint.set(StrUtil.EMPTY);
                } else if (split.length > 1) {
                    shell.currentPrint.set(split[1]);
                }
            }
        }
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
        if (msg.contains(shell.currentPrint.get()) && !msg.contains(CRLF)) {
            msg = msg.replaceAll("\u0007", "").trim();
            shell.remoteCmd.set(msg);
            shell.localLastCmd.set(msg);
            shell.currentPrint.set(msg);
            shell.clearShellLineWithPrompt();
            Printer.print(msg);
            return;
        } else if (msg.contains(shell.localLastInput) && !msg.equals(shell.localLastInput)) {
            String tmp = msg.substring(shell.localLastInput.length());
            shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
            shell.localLastCmd.getAndUpdate(prev -> prev + tmp);
            shell.currentPrint.getAndUpdate(prev -> prev + tmp);
            Printer.print(tmp);
            return;
        } else {
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
                String[] split = msg.split("\u0007");
                if (split.length == 1) {
                    if (split[0].equals(shell.localLastInput)) {
                        return;
                    }
                } else if (split.length == 2) {
                    msg = split[1];
                    if (!msg.contains(CRLF)) {
                        printer.print(msg);
                        String tmp = msg;
                        shell.remoteCmd.getAndUpdate(prev -> prev + tmp);
                        shell.localLastCmd.getAndUpdate(prev -> prev.replaceAll("\t", "") + tmp);
                        shell.currentPrint.getAndUpdate(prev -> prev + tmp);
                        return;
                    }
                } else {
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
                shell.currentPrint.getAndUpdate(prev -> prev + tmp);
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
                    shell.clearShellLineWithPrompt();
                    msg = split[split.length - 1];
                    if (msg.split("#").length == 2) {
                        shell.remoteCmd.set(msg.split("#")[1].trim());
                        shell.localLastCmd.set(msg.split("#")[1].trim());
                    }
                } else {
                    for (String input : split) {
                        if (StringUtils.isEmpty(input)) {
                            continue;
                        }
                        if (input.split("#").length == 2) {
                            shell.remoteCmd.set(input.split("#")[1].trim());
                            shell.localLastCmd.set(msg.split("#")[1].trim());
                        }
                        if (shell.tabFeedbackRec.contains(input)) {
                            continue;
                        }
                        printer.print(CRLF + input);
                        shell.tabFeedbackRec.add(input);
                    }
                    return;
                }
            }
            if (msg.startsWith(CRLF)) {
                msg = msg.replaceFirst("\r\n", "");
            }
        }


        String tmp = msg;
        shell.currentPrint.getAndUpdate(prev -> prev + tmp);
        printer.print(msg);
    }
}
