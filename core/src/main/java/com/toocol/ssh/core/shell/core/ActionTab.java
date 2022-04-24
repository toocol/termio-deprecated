package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.action.AbstractCharAction;
import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.common.utils.CharUtil;
import com.toocol.ssh.common.utils.Tuple2;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:45
 */
public final class ActionTab extends AbstractCharAction<Shell> {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.TAB};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.bottomLinePrint.get().contains(shell.prompt.get())) {
            Tuple2<Integer, Integer> cursorPosition = shell.term.getCursorPosition();
            shell.term.setCursorPosition(shell.currentPrint.length() + shell.prompt.get().length(), cursorPosition._2());
        }

        if (shell.status.equals(Shell.Status.NORMAL)) {
            shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(shell.cmd);
            shell.remoteCmd.delete(0, shell.remoteCmd.length()).append(shell.cmd);
        }
        shell.localLastInput.delete(0, shell.localLastInput.length()).append(localLastInputBuffer);
        localLastInputBuffer.delete(0, localLastInputBuffer.length());
        shell.tabFeedbackRec.clear();
        shell.writeAndFlush(shell.cmd.append(CharUtil.TAB).toString().getBytes(StandardCharsets.UTF_8));
        shell.cmd.delete(0, shell.cmd.length());
        shell.status = Shell.Status.TAB_ACCOMPLISH;
        return false;
    }
}
