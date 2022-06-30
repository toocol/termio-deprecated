package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.utils.Tuple2;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toocol.ssh.utilities.utils.StrUtil.CRLF;
import static com.toocol.ssh.utilities.utils.StrUtil.SPACE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
record ShellPrinter(Shell shell) {

    private static final PrintStream printer = Printer.PRINTER;
    private static final Console console = Console.get();
    public static final Pattern PROMPT_ECHO_PATTERN = Pattern.compile("(\\[(\\w*?)@(.*?)][$#]) .*");

    void printErr(String msg) {
        printer.print(msg);
        printer.print(shell.getPrompt());
    }

    boolean printInNormal(String msg) {
        if (msg.contains("export HISTCONTROL=ignoreboth")) {
            return false;
        }
        if (shell.localLastCmd.toString().equals(msg)) {
            return false;
        } else if (msg.startsWith("\b\u001B[K")) {
            String[] split = msg.split("\r\n");
            if (split.length == 1) {
                return false;
            }
            msg = split[1];
        } else if (msg.startsWith(shell.localLastCmd.toString().trim())) {
            // cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(shell.localLastCmd.toString().trim().length());
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
                    shell.currentPrint.delete(0, shell.currentPrint.length());
                } else if (split.length > 1) {
                    shell.currentPrint.delete(0, shell.currentPrint.length()).append(split[1]);
                }
            }

            if (!tmp.contains(CRLF)) {
                Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
                if (cursorPosition._1() != 0) {
                    shell.term.setCursorPosition(0, cursorPosition._2());
                }
            }
        }

        if (msg.contains(CRLF)) {
            String[] split = msg.split(CRLF);
            shell.bottomLinePrint = split[split.length - 1];
        } else {
            shell.bottomLinePrint = msg;
        }

        msg = console.processAnisControl(msg);
        printer.print(msg);
        return true;
    }

    void printInVim(String msg) {
        if (shell.localLastInput.toString().trim().equals(msg.replaceAll("\r\n", ""))) {
            return;
        }
        if (shell.selectHistoryCmd.toString().trim().equals(msg.replaceAll("\r\n", ""))) {
            return;
        }
        if (msg.contains(CRLF)) {
            String[] split = msg.split(CRLF);
            shell.bottomLinePrint = split[split.length - 1];
        } else {
            shell.bottomLinePrint = msg;
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
        if (StringUtils.isNotEmpty(shell.currentPrint)
                && msg.contains(shell.currentPrint)
                && !msg.replaceAll("\u0007", "").equals(shell.currentPrint.toString())
                && !msg.contains(CRLF)) {
            String tmp = msg.replaceAll("\u0007", "");
            shell.remoteCmd.delete(0, shell.remoteCmd.length()).append(tmp);
            shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(tmp);
            shell.currentPrint.delete(0, shell.currentPrint.length()).append(tmp);
            if (!msg.contains("]# ")) {
                shell.clearShellLineWithPrompt();
            }
            Printer.print(msg);
            return;
        } else if (StringUtils.isNotEmpty(shell.localLastInput.toString())
                && msg.startsWith(shell.localLastInput.toString())
                && !msg.replaceAll("\u0007", "").equals(shell.localLastInput.toString())
                && !msg.contains(CRLF)) {
            msg = msg.replaceAll("\u0007", "");
            String tmp = msg.substring(shell.localLastInput.length());
            shell.remoteCmd.append(tmp);
            shell.localLastCmd.append(tmp);
            shell.currentPrint.append(tmp);
            Printer.print(tmp);
            return;
        } else {
            msg = msg.replaceAll("\b", "");
            if (msg.trim().equals(shell.localLastCmd.toString().replaceAll("\t", ""))) {
                if (msg.endsWith(SPACE)) {
                    printer.print(SPACE);
                }
                return;
            }
            if (msg.equals(shell.localLastInput.toString())) {
                return;
            }

            // remove system prompt voice
            if (msg.contains("\u0007")) {
                String[] split = msg.split("\u0007");
                if (split.length == 1) {
                    if (split[0].equals(shell.localLastInput.toString())) {
                        return;
                    }
                } else if (split.length == 2) {
                    msg = split[1];
                    if (!msg.contains(CRLF)) {
                        printer.print(msg);
                        shell.remoteCmd.append(msg);
                        String newVal = shell.localLastCmd.toString().replaceAll("\t", "") + msg;
                        shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(newVal);
                        shell.currentPrint.append(msg);
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
                shell.remoteCmd.append(msg);
                String newVal = shell.localLastCmd.toString().replaceAll("\t", "") + msg;
                shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(newVal);
                shell.currentPrint.append(msg);
                return;
            }

            String[] split = msg.split("\r\n");
            if (split.length != 0) {
                shell.bottomLinePrint = split[split.length - 1];
                for (String input : split) {
                    if (StringUtils.isEmpty(input)) {
                        continue;
                    }
                    if (input.split("#").length == 2) {
                        shell.remoteCmd.delete(0, shell.remoteCmd.length()).append(input.split("#")[1].trim());
                        shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(msg.split("#")[1].trim());
                    }
                    if (shell.tabFeedbackRec.contains(input)) {
                        continue;
                    }
                    printer.print(CRLF + input);
                    shell.tabFeedbackRec.add(input);
                }
                return;
            }
            if (msg.startsWith(CRLF)) {
                msg = msg.replaceFirst("\r\n", "");
            }
        }

        shell.bottomLinePrint = msg;

        shell.currentPrint.append(msg);
        printer.print(msg);
    }

    void printInMore(String msg) {
        if (shell.localLastInput.toString().trim().equals(msg.replaceAll("\r\n", ""))) {
            return;
        }
        if (shell.selectHistoryCmd.toString().trim().equals(msg.replaceAll("\r\n", ""))) {
            return;
        }
        if (msg.contains(CRLF)) {
            String[] split = msg.split(CRLF);
            shell.bottomLinePrint = split[split.length - 1];
        } else {
            shell.bottomLinePrint = msg;
        }
        printer.print(msg);
    }
}
