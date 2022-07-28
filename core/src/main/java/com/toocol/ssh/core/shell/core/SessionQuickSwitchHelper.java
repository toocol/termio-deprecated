package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:39
 */
public class SessionQuickSwitchHelper {

    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final Shell shell;

    public SessionQuickSwitchHelper(Shell shell) {
        this.shell = shell;
    }

    public void initialize() {

    }

    public void switchSession() {
        shell.status = Shell.Status.QUICK_SWITCH;

        shell.status = Shell.Status.NORMAL;
    }

    private void printSwitchPanel() {

    }
}
