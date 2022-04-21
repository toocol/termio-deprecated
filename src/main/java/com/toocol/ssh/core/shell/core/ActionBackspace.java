package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.action.AbstractCharAction;
import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.core.Printer;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:46
 */
public final class ActionBackspace extends AbstractCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.BACKSPACE};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
        if (cursorPosition._1() <= shell.prompt.get().length()) {
            Printer.voice();
            shell.status = Shell.Status.NORMAL;
            return false;
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
        return false;
    }
}
