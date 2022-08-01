package com.toocol.termio.core.shell.core;

import com.toocol.termio.utilities.anis.AsciiControl;
import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.utils.CharUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toocol.termio.utilities.utils.StrUtil.*;

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
        if (lastCmd.equals("top")) {
            Printer.print(msg);
            return true;
        }
//        Shell.CONSOLE.rollingProcessing(msg);
        if (StringUtils.isEmpty(lastCmd) && clean(msg).startsWith(AsciiControl.LF)) {
            msg = msg.replaceFirst(AsciiControl.LF, "");
        }
        if (shell.localLastCmd.toString().trim().equals(msg.trim())) {
            return false;
        } else if (msg.startsWith(lastCmd) && StringUtils.isNotEmpty(lastCmd)) {
            // SSH: cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(lastCmd.length());
        }
        if (msg.startsWith(splitChar)) {
            msg = msg.replaceFirst(splitChar, "");
        }
        if (shell.protocol.equals(ShellProtocol.MOSH)) {
            StringBuilder sb = new StringBuilder();
            for (String str : msg.split(splitChar)) {
                if ((str.equals(lastCmd) && StringUtils.isNotEmpty(lastCmd)) || str.contains(AsciiControl.BEL)) {
                    continue;
                }
                str = shell.fillPrompt(str);
                sb.append(str).append("\n");
            }
            if (sb.length() > 0 && sb.toString().contains(shell.getPrompt())) {
                sb.deleteCharAt(sb.length() - 1);
            }
            if (!msg.endsWith(AsciiControl.LF) && sb.charAt(sb.length() - 1) == CharUtil.LF) {
                sb.deleteCharAt(sb.length() - 1);
            }
            msg = sb.toString();
        }

        if (lastCmd.equals("clear") && msg.contains("\u001B")) {
            msg = EMPTY;
        }
        String tmp = msg;
        if (tmp.contains(shell.prompt.get())) {

            if (tmp.contains(splitChar)) {
                String[] split = tmp.split(splitChar);
                for (int i = 0; i < split.length; i++) {
                    Matcher matcher = PROMPT_ECHO_PATTERN.matcher(split[i]);
                    if (matcher.find() && i == split.length - 1) {
                        String promptAndEcho = matcher.group(0);
                        String[] splitPrompt = promptAndEcho.split("# ");
                        if (splitPrompt.length == 1) {
                            shell.currentPrint.delete(0, shell.currentPrint.length());
                        } else if (splitPrompt.length > 1) {
                            StringBuffer currentPrint = shell.currentPrint.delete(0, shell.currentPrint.length());
                            if (splitPrompt[1].startsWith(lastCmd)) {
                                splitPrompt[1] = splitPrompt[1].replaceFirst(lastCmd, "");
                            }
                            String clean = clean(splitPrompt[1]);
                            if (!"^C".equals(clean) && !lastCmd.equals(clean) && !shell.prompt.get().startsWith(clean)) {
                                currentPrint.append(clean);
                            }
                        }
                    } else if (matcher.find()) {
                        shell.currentPrint.delete(0, shell.currentPrint.length());
                    }
                }
            } else {
                Matcher matcher = PROMPT_ECHO_PATTERN.matcher(msg);
                if (matcher.find()) {
                    String promptAndEcho = matcher.group(0);
                    String[] split = promptAndEcho.split("# ");
                    if (split.length == 1) {
                        shell.currentPrint.delete(0, shell.currentPrint.length());
                    } else if (split.length > 1) {
                        StringBuffer currentPrint = shell.currentPrint.delete(0, shell.currentPrint.length());
                        String clean = clean(split[1]);
                        if (!"^C".equals(clean) && !(lastCmd.startsWith("cd"))) {
                            currentPrint.append(clean);
                        }
                    }
                }

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
        if (shell.localLastCmd.toString().equals(Shell.RESIZE_COMMAND)) {
            Printer.print(shell.currentPrint.toString());
        }
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
        boolean bel = msg.contains(AsciiControl.BEL);
        msg = msg.replaceAll(AsciiControl.BEL, EMPTY);
        msg = msg.replaceAll(AsciiControl.BS, EMPTY);
        if (shell.protocol.equals(ShellProtocol.MOSH)) {
            msg = msg.replaceAll("25l", EMPTY);
        }
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        if (msg.contains(AsciiControl.ESCAPE) && shell.protocol.equals(ShellProtocol.SSH)) {
            if (bel) Printer.bel();
            return;
        }
        if (msg.equals(shell.tabAccomplishLastStroke)) {
            if (bel) Printer.bel();
            return;
        }
        String splitChar = shell.getProtocol().equals(ShellProtocol.SSH) ? CRLF : LF;
        if (StringUtils.isNotEmpty(shell.currentPrint)
                && msg.contains(shell.currentPrint)
                && !msg.equals(shell.currentPrint.toString())
                && !msg.contains(splitChar)) {
            shell.remoteCmd.delete(0, shell.remoteCmd.length()).append(clean(msg));
            shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(clean(msg));
            shell.currentPrint.delete(0, shell.currentPrint.length()).append(clean(msg));
            if (!msg.contains("]# ")) {
                shell.clearShellLineWithPrompt();
            }
            if (bel) Printer.bel();
            Printer.print(msg);
            return;
        } else if (StringUtils.isNotEmpty(shell.localLastInput.toString())
                && msg.startsWith(shell.localLastInput.toString())
                && !msg.equals(shell.localLastInput.toString())
                && !msg.contains(splitChar)) {
            String tmp = msg.substring(shell.localLastInput.length());
            shell.remoteCmd.append(clean(tmp));
            shell.localLastCmd.append(clean(tmp));
            shell.currentPrint.append(clean(tmp));
            if (bel) Printer.bel();
            Printer.print(tmp);
            return;
        } else {
            if (msg.trim().equals(shell.localLastCmd.toString().replaceAll("\t", ""))) {
                if (msg.endsWith(SPACE)) {
                    if (bel) Printer.bel();
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
                    if (!msg.contains(splitChar)) {
                        Printer.print(msg);
                        shell.remoteCmd.append(msg);
                        String newVal = shell.localLastCmd.toString().replaceAll("\t", "") + msg;
                        shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(clean(newVal));
                        shell.currentPrint.append(clean(msg));
                        return;
                    }
                } else {
                    return;
                }
            }
            if (StringUtils.isEmpty(msg)) {
                return;
            }
            if (!msg.contains(splitChar)) {
                if (bel) Printer.bel();
                Printer.print(msg);
                shell.remoteCmd.append(clean(msg));
                String newVal = shell.localLastCmd.toString().replaceAll("\t", "") + msg;
                shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(clean(newVal));
                shell.currentPrint.append(clean(msg));
                return;
            }

            String[] split = msg.split(splitChar);
            if (split.length != 0) {
                shell.bottomLinePrint = split[split.length - 1];
                for (String input : split) {
                    if (StringUtils.isEmpty(input)) {
                        continue;
                    }
                    if (input.split("#").length == 2) {
                        shell.remoteCmd.delete(0, shell.remoteCmd.length()).append(clean(input.split("#")[1].trim()));
                        shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(clean(msg.split("#")[1].trim()));
                    }
                    if (shell.tabFeedbackRec.contains(input)) {
                        continue;
                    }
                    Printer.print(CRLF + input);
                    shell.tabFeedbackRec.add(clean(input));
                }
                return;
            }
            if (msg.startsWith(splitChar)) {
                msg = msg.replaceFirst(splitChar, "");
            }
        }

        shell.bottomLinePrint = msg;

        shell.currentPrint.append(clean(msg));
        if (bel) Printer.bel();
        Printer.print(msg);
    }

    void printInMore(String msg) {
        String splitChar = shell.getProtocol().equals(ShellProtocol.SSH) ? CRLF : LF;
        if (shell.localLastInput.toString().trim().equals(msg.replaceAll(splitChar, ""))) {
            return;
        }
        if (shell.selectHistoryCmd.toString().trim().equals(msg.replaceAll(splitChar, ""))) {
            return;
        }
        if (msg.contains(splitChar)) {
            String[] split = msg.split(splitChar);
            shell.bottomLinePrint = split[split.length - 1];
        } else {
            shell.bottomLinePrint = msg;
        }
        Printer.print(msg);
    }

    private String clean(String str) {
        return AsciiControl.clean(str);
    }
}
