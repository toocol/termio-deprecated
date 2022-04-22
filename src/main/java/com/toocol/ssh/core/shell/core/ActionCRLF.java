package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.action.AbstractCharAction;
import com.toocol.ssh.common.event.CharEvent;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.term.core.Printer;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:44
 */
public final class ActionCRLF extends AbstractCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.CR, CharEvent.LF};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.status.equals(Shell.Status.TAB_ACCOMPLISH)) {
            shell.localLastCmd.delete(0, shell.localLastCmd.length()).append(shell.remoteCmd).append(StrUtil.CRLF);
        }
        shell.localLastInput.delete(0, shell.localLastInput.length()).append(localLastInputBuffer);
        shell.lastRemoteCmd.delete(0, shell.lastRemoteCmd.length()).append(shell.remoteCmd.toString());
        shell.lastExecuteCmd.delete(0, shell.lastExecuteCmd.length())
                .append(StringUtils.isEmpty(shell.remoteCmd) ? shell.cmd.toString() : shell.remoteCmd.toString().replaceAll("\b", ""));
        if (!StrUtil.EMPTY.equals(shell.lastExecuteCmd.toString()) && (shell.status == Shell.Status.NORMAL || shell.status == Shell.Status.TAB_ACCOMPLISH)) {
            shell.historyCmdHelper.push(shell.lastExecuteCmd.toString());
        }
        if (remoteCursorOffset) {
            shell.cmd.delete(0, shell.cmd.length());
        }
        Printer.print(StrUtil.CRLF);
        shell.status = Shell.Status.NORMAL;

        return true;
    }
}
