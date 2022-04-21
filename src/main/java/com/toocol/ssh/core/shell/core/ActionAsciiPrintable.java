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
        if (cursorPosition._1() < shell.currentPrint.get().length() + shell.prompt.get().length()) {
            // cursor has moved
            int index = cursorPosition._1() - shell.prompt.get().length();
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                String removal = "\u007F".repeat(shell.remoteCmd.get().length());
                shell.remoteCmd.getAndUpdate(prev -> prev.insert(index, inChar));
                shell.localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()).append(shell.remoteCmd.get()));
                removal += shell.remoteCmd.get().toString();
                shell.writeAndFlush(removal.getBytes(StandardCharsets.UTF_8));
                remoteCursorOffset = true;
            } else {
                shell.cmd.insert(index, inChar);
                localLastInputBuffer.insert(index, inChar);
            }
            shell.currentPrint.getAndUpdate(prev -> prev.insert(index, inChar));
            shell.term.hideCursor();
            Printer.print(shell.currentPrint.get().substring(index, shell.currentPrint.get().length()));
            shell.term.setCursorPosition(cursorPosition._1() + 1, cursorPosition._2());
            shell.term.showCursor();
        } else {
            // normal print
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                shell.remoteCmd.getAndUpdate(prev -> prev.append(inChar));
                shell.localLastCmd.getAndUpdate(prev -> prev.append(inChar));
                shell.writeAndFlush(inChar);
            } else {
                shell.cmd.append(inChar);
            }
            shell.currentPrint.getAndUpdate(prev -> prev.append(inChar));
            localLastInputBuffer.append(inChar);
            Printer.print(String.valueOf(inChar));
        }
        return false;
    }
}
