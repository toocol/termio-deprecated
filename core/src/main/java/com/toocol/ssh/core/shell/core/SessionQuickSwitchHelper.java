package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermTheme;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.anis.AsciiControl;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.execeptions.IStacktraceParser;
import com.toocol.ssh.utilities.functional.Switchable;
import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.utils.StrUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.toocol.ssh.core.ssh.SshAddress.ESTABLISH_SSH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:39
 */
public final class SessionQuickSwitchHelper implements Loggable, IStacktraceParser {
    private static final int VIEWPORT_LEN = 7;
    private static final int SCROLLABLE = VIEWPORT_LEN / 2 + 1;
    private static final int[] PART_PROPORTION = new int[]{1, 3, 2, 2, 2};
    private static final String[] PART_HEADS = new String[]{"   No.", "address", "path", "protocol", "status"};
    private static final String HELP_INFO = " Press '↑'/'↓' key to choose session to switch, '←'/'→' to change group, 'Enter' to confirm, 'Esc' to quit.";
    private static final Term term = Term.getInstance();
    private static final TermTheme theme = Term.theme;

    private final int[] recordCursorPos = new int[2];
    private final CredentialCache credentialCache = CredentialCache.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final Shell shell;

    private volatile boolean quit;
    private volatile boolean changeSession;

    private Switchable[] switchableList;
    private Switchable chosen;
    private int indicator;
    private int viewportStart;
    private int helpInfoLine;
    private int bottomLineOffset;

    public SessionQuickSwitchHelper(Shell shell) {
        this.shell = shell;
    }

    public void initialize() {
        Collection<Switchable> credentialSwitchable = credentialCache.getAllSwitchable()
                .stream()
                .filter(switchable -> !switchable.uri().equals(shell.uri()))
                .toList();
        Collection<Switchable> sessionSwitchable = sshSessionCache.getAllSwitchable()
                .stream()
                .filter(switchable -> !switchable.uri().equals(shell.uri()))
                .toList();
        Set<Switchable> removalSet = new HashSet<>(sessionSwitchable);
        removalSet.addAll(credentialSwitchable);
        switchableList = new Switchable[removalSet.size()];
        int idx = 0;
        for (Switchable switchable : removalSet) {
            switchableList[idx++] = switchable;
        }
        Arrays.sort(switchableList);
        reset();
    }

    public boolean switchSession() {
        try {
            shell.status = Shell.Status.QUICK_SWITCH;
            reset();

            int[] cursorPosition = term.getCursorPosition();
            int offset = 0;
            int height = term.getHeight();
            if (cursorPosition[1] > height - bottomLineOffset + 1) {
                for (; offset < bottomLineOffset - 1; offset++) {
                    Printer.println(AsciiControl.ANIS_ERASE_LINE);
                }
            }

            AnisStringBuilder promptBase = new AnisStringBuilder()
                    .append(shell.getPrompt())
                    .front(theme.lcCmdExecuteHighlightColor)
                    .append("> {} <");

            cursorPosition = term.getCursorPosition();
            term.hideCursor();
            recordCursorPos[0] = shell.getPrompt().length();
            recordCursorPos[1] = cursorPosition[1] - 1 - offset;
            term.setCursorPosition(0, recordCursorPos[1] + helpInfoLine);
            int width = term.getWidth();
            Printer.print(
                    new AnisStringBuilder().background(theme.switchSessionPanelBottomBgColor)
                            .append(HELP_INFO)
                            .space(width - HELP_INFO.length())
                            .toString()
            );

            while (!quit) {
                term.setCursorPosition(0, recordCursorPos[1]);
                Printer.println(StrUtil.fullFillParam(promptBase.toString(), indicator + viewportStart));
                term.setCursorPosition(0, recordCursorPos[1] + 1);
                printSwitchPanel();
                term.setCursorPosition(24, recordCursorPos[1] + 1);

                shell.shellReader.readCmd();
            }

            cleanSwitchPanel();
            term.setCursorPosition(recordCursorPos[0], recordCursorPos[1]);
            term.showCursor();
            shell.status = Shell.Status.NORMAL;
        } catch (Exception e) {
            error("Catch exception when quick switch session, stackTrace = {}", parseStackTrace(e));
        }
        return changeSession;
    }

    public void upSession() {
        if (indicator > SCROLLABLE || (indicator > 1 && viewportStart == 0)) {
            indicator--;
            return;
        }
        if (viewportStart > 0) {
            viewportStart--;
        }
    }

    public void downSession() {
        if (indicator < SCROLLABLE || (viewportStart + indicator + SCROLLABLE > switchableList.length && viewportStart + indicator < switchableList.length)) {
            indicator++;
            return;
        }
        if (viewportStart + VIEWPORT_LEN < switchableList.length) {
            viewportStart++;
        }
    }

    public void changeSession() {
        if (chosen == null) {
            return;
        }
        String[] uri = chosen.uri().split("@");
        int index = credentialCache.indexOf(uri[1], uri[0]);
        shell.getVertx().executeBlocking(promise -> {
            while (true) {
                if (StatusCache.SWITCH_SESSION_WAIT_HANG_PREVIOUS) {
                    shell.getEventBus().send(ESTABLISH_SSH_SESSION.address(), index);
                    StatusCache.SWITCH_SESSION_WAIT_HANG_PREVIOUS = false;
                    break;
                }
            }
            promise.complete();
        });
        quit();
        changeSession = true;
    }

    public void quit() {
        this.quit = true;
    }

    private void reset() {
        this.changeSession = false;
        this.quit = false;
        this.indicator = 1;
        this.viewportStart = 0;
        this.chosen = null;
        this.helpInfoLine = Math.min(VIEWPORT_LEN + 2, switchableList.length + 2);
        this.bottomLineOffset = Math.min(VIEWPORT_LEN + 3, switchableList.length + 3);
    }

    private void printSwitchPanel() {
        AnisStringBuilder builder = new AnisStringBuilder();
        int[] partLength = new int[5];
        int width = term.getWidth();
        int totalPartLength = 0;
        for (int i = 0; i < 5; i++) {
            partLength[i] = width * PART_PROPORTION[i] / 10;
            builder.background(theme.switchSessionHeadBgColor)
                    .front(theme.switchSessionHeadFrontColor)
                    .append(PART_HEADS[i])
                    .space(partLength[i] - PART_HEADS[i].length());
            totalPartLength += partLength[i];
        }
        Printer.println(builder.space(width - totalPartLength).toString());
        builder.clearStr().clearColor();
        int range = Math.min(viewportStart + VIEWPORT_LEN, switchableList.length);
        for (int i = viewportStart; i < range; i++) {
            Switchable switchable = switchableList[i];
            int idx = i + 1;
            String prefix;
            boolean chosenSession = indicator + viewportStart == idx;
            if (chosenSession) {
                chosen = switchable;
                prefix = " > ";
            } else if (idx != switchableList.length && i == range - 1) {
                prefix = " ..";
            } else {
                prefix = "   ";
            }
            String index = "[" + (idx < 10 ? "0" + idx : idx) + "]";
            String uri = switchable.uri();
            String curPath = switchable.currentPath();
            String protocol = switchable.protocol();
            String alive = switchable.alive() ? "alive" : "disconnect";
            builder.background(chosenSession ? theme.switchSessionChosenBgColor : theme.switchSessionPanelBodyBgColor)
                    .front(chosenSession ? theme.switchSessionChosenFrontColor : -1)
                    .append(prefix)
                    .append(index).space(partLength[0] - (prefix + index).length())
                    .append(uri).space(partLength[1] - uri.length())
                    .append(curPath).space(partLength[2] - curPath.length())
                    .append(protocol).space(partLength[3] - protocol.length())
                    .append(alive).space(partLength[4] - alive.length())
                    .space(width - totalPartLength).crlf();
            Printer.print(builder.toString());
            builder.clearStr().clearColor();
        }
    }

    private void cleanSwitchPanel() {
        term.setCursorPosition(0, recordCursorPos[1]);
        for (int i = 0; i < bottomLineOffset; i++) {
            if (i == 0) {
                Printer.print(AsciiControl.ANIS_ERASE_LINE);
                Printer.println(shell.getPrompt());
            } else {
                Printer.println(AsciiControl.ANIS_ERASE_LINE);
            }
        }
    }
}
