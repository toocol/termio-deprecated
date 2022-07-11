package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.anis.AsciiControl;
import com.toocol.ssh.utilities.anis.Printer;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toocol.ssh.utilities.utils.StrUtil.*;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/6 13:05
 */
record ShellPrinter(Shell shell) {

    public static final Pattern PROMPT_ECHO_PATTERN = Pattern.compile("(\\[(\\w*?)@(.*?)][$#]) .*");

    void printErr(String msg) {
        Printer.print(msg);
        Printer.print(shell.getPrompt());
    }

    boolean printInNormal(String msg) {
        if (msg.contains("export HISTCONTROL=ignoreboth")) {
            return false;
        }
        String splitChar = shell.getProtocol().equals(ShellProtocol.SSH) ? CRLF : LF;
        String lastCmd = shell.localLastCmd.toString().trim();
        if (shell.localLastCmd.toString().equals(msg)) {
            return false;
        } else if (msg.startsWith("\b\u001B[K")) {
            String[] split = msg.split(splitChar);
            if (split.length == 1) {
                return false;
            }
            msg = split[1];
        } else if (msg.startsWith(lastCmd) && StringUtils.isNotEmpty(lastCmd)) {
            // SSH: cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(shell.localLastCmd.toString().length());
        }
        if (msg.startsWith(splitChar)) {
            msg = msg.replaceFirst(splitChar, "");
        }
        if (shell.protocol.equals(ShellProtocol.MOSH)) {
            StringBuilder sb = new StringBuilder();
            for (String str : msg.split(splitChar)) {
                if (str.contains(lastCmd) && StringUtils.isNotEmpty(lastCmd)) {
                    continue;
                }
                sb.append(str).append("\n");
            }
            if (sb.length() > 0 && sb.toString().contains(shell.getPrompt())) {
                sb.deleteCharAt(sb.length() - 1);
            }
            msg = sb.toString();
        }

        if (lastCmd.equals("clear") && msg.equals("\u001B\u0012\u0019\"\u0017\u001B[?25l\r\u001B[K\u001B[1;22H\n")) {
            msg = EMPTY;
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

            if (!tmp.contains(splitChar)) {
                int[] cursorPosition = shell.term.getCursorPosition();
                if (cursorPosition[0] != 0) {
                    shell.term.setCursorPosition(0, cursorPosition[1]);
                }
            }
        }

        if (msg.contains(splitChar)) {
            String[] split = msg.split(splitChar);
            shell.bottomLinePrint = split[split.length - 1];
        } else {
            shell.bottomLinePrint = msg;
        }

        Printer.print(msg);
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
        Printer.print(msg);
    }

    void printInTabAccomplish(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (msg.contains(AsciiControl.ESCAPE) && shell.protocol.equals(ShellProtocol.SSH)) {
            return;
        }
        msg = msg.replaceAll("\b", "");
        if (StringUtils.isNotEmpty(shell.currentPrint)
                && msg.contains(shell.currentPrint)
                && !msg.replaceAll(AsciiControl.BEL, "").equals(shell.currentPrint.toString())
                && !msg.contains(CRLF)) {
            String tmp = msg.replaceAll(AsciiControl.BEL, "");
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
                && !msg.replaceAll(AsciiControl.BEL, "").equals(shell.localLastInput.toString())
                && !msg.contains(CRLF)) {
            msg = msg.replaceAll(AsciiControl.BEL, "");
            String tmp = msg.substring(shell.localLastInput.length());
            shell.remoteCmd.append(tmp);
            shell.localLastCmd.append(tmp);
            shell.currentPrint.append(tmp);
            Printer.print(tmp);
            return;
        } else {
            if (msg.trim().equals(shell.localLastCmd.toString().replaceAll("\t", ""))) {
                if (msg.endsWith(SPACE)) {
                    Printer.print(SPACE);
                }
                return;
            }
            if (msg.equals(shell.localLastInput.toString())) {
                return;
            }

            // remove system prompt voice
            if (msg.contains(AsciiControl.BEL)) {
                String[] split = msg.split(AsciiControl.BEL);
                if (split.length == 1) {
                    if (split[0].equals(shell.localLastInput.toString())) {
                        return;
                    }
                } else if (split.length == 2) {
                    msg = split[1];
                    if (!msg.contains(CRLF)) {
                        Printer.print(msg);
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
                Printer.print(msg);
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
                    Printer.print(CRLF + input);
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
        Printer.print(msg);
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
        Printer.print(msg);
    }
}
