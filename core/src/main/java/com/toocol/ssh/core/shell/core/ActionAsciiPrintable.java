package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.ASCIIStrCache;
import com.toocol.ssh.utilities.utils.CharUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:43
 */
public final class ActionAsciiPrintable extends ShellCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.ASCII_PRINTABLE};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.escapeHelper.isAcceptBracketAfterEscape()) {
            return false;
        }
        if (inChar == CharUtil.SPACE && shell.currentPrint.length() == 0) {
            return false;
        }
        int[] cursorPosition = shell.term.getCursorPosition();
        if (cursorPosition[0] < shell.currentPrint.length() + shell.prompt.get().length()) {
            // cursor has moved
            int index = cursorPosition[0] - shell.prompt.get().length();
            if (index == 0 && inChar == CharUtil.SPACE) {
                return false;
            }
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                String removal = "\u007F".repeat(shell.remoteCmd.length());
                shell.remoteCmd.insert(index, inChar);
                shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(shell.remoteCmd);
                shell.tabAccomplishLastStroke = ASCIIStrCache.toString(inChar);
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
            shell.term.setCursorPosition(cursorPosition[0] + 1, cursorPosition[1]);
            shell.term.showCursor();
        } else {
            // cursor hasn't moved
            if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
                shell.remoteCmd.append(inChar);
                shell.localLastCmd.append(inChar);
                shell.tabAccomplishLastStroke = ASCIIStrCache.toString(inChar);
                shell.writeAndFlush(inChar);
            } else {
                shell.cmd.append(inChar);
            }
            shell.currentPrint.append(inChar);
            localLastInputBuffer.append(inChar);

            Printer.print(ASCIIStrCache.toString(inChar));
        }
        return false;
    }
}
