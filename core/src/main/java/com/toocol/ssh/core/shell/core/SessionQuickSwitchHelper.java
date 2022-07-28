package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.utilities.execeptions.IStacktraceParser;
import com.toocol.ssh.utilities.functional.Switchable;
import com.toocol.ssh.utilities.log.Loggable;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:39
 */
public class SessionQuickSwitchHelper implements Loggable, IStacktraceParser {
    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final Set<Switchable> switchableSet = new TreeSet<>();
    private final Shell shell;

    private volatile boolean quit;

    private int indicator;

    public SessionQuickSwitchHelper(Shell shell) {
        this.shell = shell;
    }

    public void initialize() {
        switchableSet.clear();
        switchableSet.addAll(credentialCache.getAllSwitchable());
        switchableSet.addAll(sshSessionCache.getAllSwitchable());
    }

    public void switchSession() {
        shell.status = Shell.Status.QUICK_SWITCH;
        try {
            while (!quit) {
                shell.shellReader.readCmd();
            }
        } catch (Exception e) {
            error("Catch exception when quick switch session, stackTrace = {}", parseStackTrace(e));
        }
        shell.status = Shell.Status.NORMAL;
    }

    public void upSession() {

    }

    public void downSession() {

    }

    public void quit() {
        this.quit = true;
    }

    private void printSwitchPanel() {

    }
}
