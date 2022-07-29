package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.execeptions.IStacktraceParser;
import com.toocol.ssh.utilities.functional.Switchable;
import com.toocol.ssh.utilities.log.Loggable;

import java.util.*;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:39
 */
public class SessionQuickSwitchHelper implements Loggable, IStacktraceParser {
    private static final int VIEWPORT_LEN = 5;
    private static final int ROLLING = 3;
    private static final Term term  = Term.getInstance();

    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final Shell shell;

    private volatile boolean quit;

    private Switchable[] switchableList;
    private int indicator;
    private int viewportStart;
    private int viewportEnd;

    public SessionQuickSwitchHelper(Shell shell) {
        this.shell = shell;
    }

    public void initialize() {
        Collection<Switchable> credentialSwitchable = credentialCache.getAllSwitchable();
        Collection<Switchable> sessionSwitchable = sshSessionCache.getAllSwitchable();
        switchableList = new Switchable[credentialSwitchable.size() + sessionSwitchable.size()];
        int idx = 0;
        for (Switchable switchable : credentialSwitchable) {
            switchableList[idx++] = switchable;
        }
        for (Switchable switchable : sessionSwitchable) {
            switchableList[idx++] = switchable;
        }
        Arrays.sort(switchableList);
        indicator = 0;
        viewportStart = 0;
        viewportEnd = Math.min(viewportStart + VIEWPORT_LEN, switchableList.length);
    }

    public void switchSession() {
        shell.status = Shell.Status.QUICK_SWITCH;
        try {
            while (!quit) {
                printSwitchPanel();
                shell.shellReader.readCmd();
            }
        } catch (Exception e) {
            error("Catch exception when quick switch session, stackTrace = {}", parseStackTrace(e));
        }
        quit = false;
        shell.status = Shell.Status.NORMAL;
    }

    public void upSession() {
        if (indicator < ROLLING) {
            return;
        }
    }

    public void downSession() {
        if (indicator < ROLLING) {
            return;
        }
    }

    public void quit() {
        this.quit = true;
    }

    private void printSwitchPanel() {
        AnisStringBuilder builder = new AnisStringBuilder();
        for (int i = viewportStart; i < viewportEnd; i++) {
            Switchable switchable = switchableList[i];
            int idx = i + 1;
            if (indicator + viewportStart == i) {
                builder.append("*");
            } else {
                builder.append(" ");
            }
            builder.append("[" + (idx < 10 ? "0" + idx : idx) + "]").tab().tab()
                    .append(switchable.uri()).tab().tab()
                    .append(switchable.currentPath()).tab().tab()
                    .append(switchable.protocol()).tab().tab()
                    .append(switchable.alive()? "alive" : "disconnect").crlf();
        }
        Printer.print(builder.toString());
    }
}
