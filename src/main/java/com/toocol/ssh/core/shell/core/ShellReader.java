package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.core.Printer;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Signal;

import java.nio.charset.StandardCharsets;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/13 2:07
 * @version: 0.0.1
 */
record ShellReader(Shell shell, ConsoleReader reader) {

    void initReader() {
        /*
         * custom handle CTRL+C
         */
        Signal.handle(new Signal("INT"), signal -> {
            try {
                shell.historyCmdHelper.reset();
                shell.cmd.delete(0, shell.cmd.length());
                shell.writeAndFlush(CharUtil.CTRL_C);
                shell.status = Shell.Status.NORMAL;
            } catch (Exception e) {
                // do nothing
            }
        });
    }

    void readCmd() throws Exception {
        shell.cmd.delete(0, shell.cmd.length());
        StringBuilder localLastInputBuffer = new StringBuilder();
        boolean remoteCursorOffset = false;
        while (true) {
            char inChar = (char) reader.readCharacter();

            /*
             * Start to deal with arrow key.
             */
            char finalChar = shell.arrowHelper.processArrowStream(inChar);

            if (shell.status.equals(Shell.Status.VIM_UNDER)) {

                char vimChar = shell.arrowHelper.processArrowBundle(finalChar, shell, reader);

                shell.writeAndFlush(shell.vimHelper.transferVimInput(vimChar));

            } else if (shell.status.equals(Shell.Status.MORE_PROC)
                    || shell.status.equals(Shell.Status.MORE_EDIT)
                    || shell.status.equals(Shell.Status.MORE_SUB)) {

                boolean support;
                switch (shell.status) {
                    case MORE_PROC -> support = shell.moreHelper.support(finalChar);
                    case MORE_SUB -> support = shell.moreHelper.supportSub(finalChar);
                    case MORE_EDIT -> support = shell.moreHelper.supportEdit(finalChar);
                    default -> support = false;
                }
                if (support) {
                    shell.writeAndFlush(finalChar);
                }

            } else {
                if (finalChar == CharUtil.UP_ARROW || finalChar == CharUtil.DOWN_ARROW) {

                    shell.status = Shell.Status.NORMAL;

                    if (finalChar == CharUtil.UP_ARROW) {
                        if (!shell.historyCmdHelper.isStart()) {
                            if (shell.cmd.length() != 0 && StringUtils.isEmpty(shell.remoteCmd.get())) {
                                shell.historyCmdHelper.pushToDown(shell.cmd.toString());
                            } else if (StringUtils.isNotEmpty(shell.remoteCmd.get())) {
                                byte[] write = "\u007F".repeat(shell.remoteCmd.get().length()).getBytes(StandardCharsets.UTF_8);
                                if (write.length > 0) {
                                    shell.writeAndFlush(write);
                                    String cmdToPush = shell.remoteCmd.get().toString().replaceAll("\u007F", "");
                                    shell.historyCmdHelper.pushToDown(cmdToPush);
                                }
                            }
                        }
                        shell.historyCmdHelper.up();
                    } else {
                        shell.historyCmdHelper.down();
                    }
                    localLastInputBuffer.delete(0, localLastInputBuffer.length()).append(shell.cmd);
                    shell.localLastCmd.set(new StringBuffer());

                } else if (finalChar == CharUtil.LEFT_ARROW || finalChar == CharUtil.RIGHT_ARROW) {

                    int cursorX = shell.term.getCursorPosition()._1();
                    if (finalChar == CharUtil.LEFT_ARROW) {
                        if (cursorX > shell.prompt.get().length()) {
                            shell.term.cursorLeft();
                        }
                    } else {
                        if (cursorX < (shell.currentPrint.get().length() + shell.prompt.get().length())) {
                            shell.term.cursorRight();
                        }
                    }

                } else if (finalChar == CharUtil.TAB) {
                    if (shell.bottomLinePrint.get().contains(shell.prompt.get())) {
                        Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
                        shell.term.setCursorPosition(shell.currentPrint.get().length() + shell.prompt.get().length(), cursorPosition._2());
                    }

                    if (shell.status.equals(Shell.Status.NORMAL)) {
                        shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.cmd));
                        shell.remoteCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.cmd));
                    }
                    shell.localLastInput.delete(0, shell.localLastInput.length()).append(localLastInputBuffer);
                    localLastInputBuffer = new StringBuilder();
                    shell.tabFeedbackRec.clear();
                    shell.writeAndFlush(shell.cmd.append(CharUtil.TAB).toString().getBytes(StandardCharsets.UTF_8));
                    shell.cmd.delete(0, shell.cmd.length());
                    shell.status = Shell.Status.TAB_ACCOMPLISH;

                } else if (finalChar == CharUtil.BACKSPACE) {
                    Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
                    if (cursorPosition._1() <= shell.prompt.get().length()) {
                        Printer.voice();
                        shell.status = Shell.Status.NORMAL;
                        continue;
                    }
                    if (cursorPosition._1() < shell.currentPrint.get().length() + shell.prompt.get().length()) {
                        // cursor has moved
                        int index = cursorPosition._1() - shell.prompt.get().length() - 1;
                        if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                            String removal = "\u007F".repeat(shell.remoteCmd.get().length());
                            shell.remoteCmd.get().deleteCharAt(index);
                            shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.remoteCmd.get()));
                            removal += shell.remoteCmd.get().toString();
                            shell.writeAndFlush(removal.getBytes(StandardCharsets.UTF_8));
                            remoteCursorOffset = true;
                        }
                        if (shell.status.equals(Shell.Status.NORMAL)) {
                            shell.cmd.deleteCharAt(index);
                        }
                        shell.currentPrint.get().deleteCharAt(index);
                        shell.term.hideCursor();
                        Printer.virtualBackspace();
                        Printer.print(shell.currentPrint.get().substring(index, shell.currentPrint.get().length()) + CharUtil.SPACE);
                        shell.term.setCursorPosition(cursorPosition._1() - 1, cursorPosition._2());
                        shell.term.showCursor();
                    } else {
                        if (localLastInputBuffer.length() > 0) {
                            localLastInputBuffer.deleteCharAt(localLastInputBuffer.length() - 1);
                        }
                        if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                            // This is ctrl+backspace
//                            shell.cmd.append('\u007F');
                            shell.writeAndFlush('\u007F');
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

                        Printer.virtualBackspace();
                    }

                } else if (finalChar == CharUtil.CR || finalChar == CharUtil.LF) {

                    if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                        shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.remoteCmd.get().toString()).append(StrUtil.CRLF));
                    }
                    shell.localLastInput.delete(0, shell.localLastInput.length()).append(localLastInputBuffer);
                    shell.lastRemoteCmd.delete(0, shell.lastRemoteCmd.length()).append(shell.remoteCmd.get().toString());
                    shell.lastExecuteCmd.delete(0, shell.lastExecuteCmd.length())
                            .append(StringUtils.isEmpty(shell.remoteCmd.get()) ? shell.cmd.toString() : shell.remoteCmd.get().toString().replaceAll("\b", ""));
                    if (!StrUtil.EMPTY.equals(shell.lastExecuteCmd.toString()) && (shell.status == Shell.Status.NORMAL || shell.status == Shell.Status.TAB_ACCOMPLISH)) {
                        shell.historyCmdHelper.push(shell.lastExecuteCmd.toString());
                    }
                    if (remoteCursorOffset) {
                        shell.cmd.delete(0, shell.cmd.length());
                    }
                    Printer.print(StrUtil.CRLF);
                    shell.status = Shell.Status.NORMAL;
                    break;

                } else if (CharUtil.isAsciiPrintable(finalChar)) {
                    if (shell.arrowHelper.isAcceptBracketAfterEscape()) {
                        continue;
                    }
                    Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
                    if (cursorPosition._1() < shell.currentPrint.get().length() + shell.prompt.get().length()) {
                        // cursor has moved
                        int index = cursorPosition._1() - shell.prompt.get().length();
                        if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                            String removal = "\u007F".repeat(shell.remoteCmd.get().length());
                            shell.remoteCmd.getAndUpdate(prev -> prev.insert(index, finalChar));
                            shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.remoteCmd.get()));
                            removal += shell.remoteCmd.get().toString();
                            shell.writeAndFlush(removal.getBytes(StandardCharsets.UTF_8));
                            remoteCursorOffset = true;
                        } else {
                            shell.cmd.insert(index, finalChar);
                            localLastInputBuffer.insert(index, finalChar);
                        }
                        shell.currentPrint.getAndUpdate(prev -> prev.insert(index, finalChar));
                        shell.term.hideCursor();
                        Printer.print(shell.currentPrint.get().substring(index, shell.currentPrint.get().length()));
                        shell.term.setCursorPosition(cursorPosition._1() + 1, cursorPosition._2());
                        shell.term.showCursor();
                    } else {
                        // normal print
                        if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                            shell.remoteCmd.getAndUpdate(prev -> prev.append(finalChar));
                            shell.localLastCmd.getAndUpdate(prev -> prev.append(finalChar));
                            shell.writeAndFlush(finalChar);
                        } else {
                            shell.cmd.append(finalChar);
                        }
                        shell.currentPrint.getAndUpdate(prev -> prev.append(finalChar));
                        localLastInputBuffer.append(finalChar);
                        Printer.print(String.valueOf(finalChar));
                    }
                }
            }
        }
    }
}
