package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.utilities.event.CharEvent;
import com.toocol.ssh.utilities.utils.CharUtil;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 20:40
 */
public final class ActionUpDownArrow extends ShellCharAction {
    @Override
    public CharEvent[] watch() {
        return new CharEvent[]{CharEvent.UP_ARROW, CharEvent.DOWN_ARROW};
    }

    @Override
    public boolean act(Shell shell, CharEvent charEvent, char inChar) {
        if (shell.status.equals(Shell.Status.QUICK_SWITCH)) {
            if (inChar == CharUtil.UP_ARROW) {
                shell.quickSwitchHelper.upSession();
            } else {
                shell.quickSwitchHelper.downSession();
            }
            return false;
        }
        shell.status = Shell.Status.NORMAL;

        if (inChar == CharUtil.UP_ARROW) {
            if (!shell.historyCmdHelper.isStart()) {
                if (shell.cmd.length() != 0 && StringUtils.isEmpty(shell.remoteCmd)) {
                    shell.historyCmdHelper.pushToDown(shell.cmd.toString());
                } else if (StringUtils.isNotEmpty(shell.remoteCmd)) {
                    byte[] write = "\u007F".repeat(shell.remoteCmd.length()).getBytes(StandardCharsets.UTF_8);
                    if (write.length > 0) {
                        shell.writeAndFlush(write);
                        String cmdToPush = shell.remoteCmd.toString().replaceAll("\u007F", "");
                        shell.historyCmdHelper.pushToDown(cmdToPush);
                    }
                }
            }
            shell.historyCmdHelper.up();
        } else {
            shell.historyCmdHelper.down();
        }
        localLastInputBuffer.delete(0, localLastInputBuffer.length()).append(shell.cmd);
        shell.localLastCmd.delete(0, shell.localLastCmd.length());
        return false;
    }
}
