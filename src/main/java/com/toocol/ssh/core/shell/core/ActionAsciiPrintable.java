package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.action.AbstractCharAction;
import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.core.Printer;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:43
 */
public final class ActionAsciiPrintable extends AbstractCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.ASCII_PRINTABLE};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.arrowHelper.isAcceptBracketAfterEscape()) {
            return false;
        }
        Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
        if (cursorPosition._1() < shell.currentPrint.length() + shell.prompt.get().length()) {
            // cursor has moved
            int index = cursorPosition._1() - shell.prompt.get().length();
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                String removal = "\u007F".repeat(shell.remoteCmd.length());
                shell.remoteCmd.insert(index, inChar);
                shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(shell.remoteCmd);
                removal += shell.remoteCmd.toString();
                shell.writeAndFlush(removal.getBytes(StandardCharsets.UTF_8));
                remoteCursorOffset = true;
            } else {
                shell.cmd.insert(index, inChar);
                localLastInputBuffer.insert(index, inChar);
            }
            shell.currentPrint.insert(index, inChar);
            shell.term.hideCursor();
            Printer.print(shell.currentPrint.substring(index, shell.currentPrint.length()));
            shell.term.setCursorPosition(cursorPosition._1() + 1, cursorPosition._2());
            shell.term.showCursor();
        } else {
            // normal print
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                shell.remoteCmd.append(inChar);
                shell.localLastCmd.append(inChar);
                shell.writeAndFlush(inChar);
            } else {
                shell.cmd.append(inChar);
            }
            shell.currentPrint.append(inChar);
            localLastInputBuffer.append(inChar);
            Printer.print(String.valueOf(inChar));
        }
        return false;
    }
}
