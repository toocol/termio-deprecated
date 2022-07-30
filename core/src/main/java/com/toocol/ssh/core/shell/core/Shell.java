package com.toocol.ssh.core.shell.core;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.core.cache.MoshSessionCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.core.shell.handlers.BlockingDfHandler;
import com.toocol.ssh.core.term.core.EscapeHelper;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.action.AbstractDevice;
import com.toocol.ssh.utilities.anis.AsciiControl;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.console.Console;
import com.toocol.ssh.utilities.execeptions.RemoteDisconnectException;
import com.toocol.ssh.utilities.functional.Executable;
import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.utils.CharUtil;
import com.toocol.ssh.utilities.utils.CmdUtil;
import com.toocol.ssh.utilities.utils.MessageBox;
import com.toocol.ssh.utilities.utils.StrUtil;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import jline.console.ConsoleReader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.toocol.ssh.core.shell.ShellAddress.START_DF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 20:57
 */
public final class Shell extends AbstractDevice implements Loggable {
    static final Pattern PROMPT_PATTERN = Pattern.compile("(\\[(\\w*?)@(.*?)][$#])");
    static final Console CONSOLE = Console.get();
    static final String RESIZE_COMMAND = AsciiControl.DC2 + CharUtil.BRACKET_START + "resize";
    final Term term = Term.getInstance();
    final ShellPrinter shellPrinter;
    final ShellReader shellReader;
    final ShellHistoryCmdHelper historyCmdHelper;
    final MoreHelper moreHelper;
    final EscapeHelper escapeHelper;
    final VimHelper vimHelper;
    final SessionQuickSwitchHelper quickSwitchHelper;
    final ShellCharEventDispatcher shellCharEventDispatcher;
    final Set<String> tabFeedbackRec = new HashSet<>();
    final StringBuilder cmd = new StringBuilder();
    /**
     * the session's id that shell belongs to.
     */
    private final long sessionId;
    /**
     * vert.x system
     */
    private final Vertx vertx;
    /**
     * the EventBus of vert.x system.
     */
    private final EventBus eventBus;
    private final String host;
    ConsoleReader reader;
    volatile StringBuffer localLastCmd = new StringBuffer();
    volatile StringBuffer remoteCmd = new StringBuffer();
    volatile StringBuffer currentPrint = new StringBuffer();
    volatile StringBuffer selectHistoryCmd = new StringBuffer();
    volatile StringBuffer localLastInput = new StringBuffer();
    volatile StringBuffer lastRemoteCmd = new StringBuffer();
    volatile StringBuffer lastExecuteCmd = new StringBuffer();
    volatile Status status = Status.NORMAL;
    volatile ShellProtocol protocol;
    volatile AtomicReference<String> prompt = new AtomicReference<>();
    volatile AtomicReference<String> fullPath = new AtomicReference<>();
    volatile StringBuilder sshWelcome = new StringBuilder();
    volatile StringBuilder moshWelcome = new StringBuilder();
    volatile String user = null;
    volatile String bottomLinePrint = StrUtil.EMPTY;
    volatile String tabAccomplishLastStroke = StrUtil.EMPTY;
    private Pattern promptCursorPattern;
    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private OutputStream outputStream;
    private InputStream inputStream;
    /**
     * JSch channel shell
     */
    private ChannelShell channelShell;
    /**
     * Mosh session
     */
    private MoshSession moshSession;
    private boolean jumpServer = false;
    private volatile boolean returnWrite = false;
    private volatile boolean promptNow = false;

    {
        try {
            reader = new ConsoleReader(System.in, null, null);
        } catch (Exception e) {
            MessageBox.setExitMessage("Create console reader failed.");
            System.exit(-1);
        }
    }

    public Shell(long sessionId, Vertx vertx, EventBus eventBus, MoshSession moshSession) {
        this.sessionId = sessionId;
        this.host = moshSession.getHost();
        this.user = moshSession.getUser();
        this.vertx = vertx;
        this.eventBus = eventBus;
        this.moshSession = moshSession;
        this.shellPrinter = new ShellPrinter(this);
        this.shellReader = new ShellReader(this, reader);
        this.historyCmdHelper = new ShellHistoryCmdHelper(this);
        this.moreHelper = new MoreHelper();
        this.escapeHelper = new EscapeHelper();
        this.vimHelper = new VimHelper();
        this.quickSwitchHelper = new SessionQuickSwitchHelper(this);
        this.shellCharEventDispatcher = new ShellCharEventDispatcher();

        this.resetIO(ShellProtocol.MOSH);
        this.shellReader.initReader();
    }

    public Shell(long sessionId, String host, String user, Vertx vertx, EventBus eventBus, ChannelShell channelShell) {
        this.sessionId = sessionId;
        this.host = host;
        this.user = user;
        this.vertx = vertx;
        this.eventBus = eventBus;
        this.channelShell = channelShell;
        this.shellPrinter = new ShellPrinter(this);
        this.shellReader = new ShellReader(this, reader);
        this.historyCmdHelper = new ShellHistoryCmdHelper(this);
        this.moreHelper = new MoreHelper();
        this.escapeHelper = new EscapeHelper();
        this.vimHelper = new VimHelper();
        this.quickSwitchHelper = new SessionQuickSwitchHelper(this);
        this.shellCharEventDispatcher = new ShellCharEventDispatcher();

        this.resetIO(ShellProtocol.SSH);
        this.shellReader.initReader();
    }

    public void resetIO(ShellProtocol protocol) {
        this.protocol = protocol;
        try {
            switch (protocol) {
                case SSH -> {
                    this.inputStream = channelShell.getInputStream();
                    this.outputStream = channelShell.getOutputStream();
                }
                case MOSH -> {
                    this.inputStream = moshSession.getInputStream();
                    this.outputStream = moshSession.getOutputStream();
                }
            }
        } catch (Exception e) {
            MessageBox.setExitMessage("Reset IO failed: " + e.getMessage());
            System.exit(-1);
        }
    }

    public boolean print(String msg) {
        Matcher matcher = PROMPT_PATTERN.matcher(msg.trim());
        if (matcher.find()) {
            String oldPrompt = prompt.get();
            prompt.set(AsciiControl.clean(matcher.group(0) + StrUtil.SPACE));
            if (!oldPrompt.equals(prompt.get())) {
                generateCursorPosPattern();
            }
            extractUserFromPrompt();
            if (status.equals(Status.VIM_UNDER)) {
                status = Status.NORMAL;
                localLastCmd.delete(0, localLastCmd.length());
            } else if (status.equals(Status.MORE_BEFORE) || status.equals(Status.MORE_PROC)
                    || status.equals(Status.MORE_EDIT) || status.equals(Status.MORE_SUB)) {
                status = Status.NORMAL;
                localLastCmd.delete(0, localLastCmd.length());
            }
        }

        if (status.equals(Status.MORE_BEFORE)) {
            status = Status.MORE_PROC;
        }

        boolean hasPrint = false;
        switch (status) {
            case NORMAL -> hasPrint = shellPrinter.printInNormal(msg);
            case TAB_ACCOMPLISH -> shellPrinter.printInTabAccomplish(msg);
            case VIM_BEFORE, VIM_UNDER -> shellPrinter.printInVim(msg);
            case MORE_BEFORE, MORE_PROC, MORE_EDIT, MORE_SUB -> shellPrinter.printInMore(msg);
            default -> {
            }
        }
        if (protocol.equals(ShellProtocol.MOSH)) {
            CONSOLE.showCursor();
        }

        if (status.equals(Shell.Status.VIM_BEFORE)) {
            status = Shell.Status.VIM_UNDER;
        }

        selectHistoryCmd.delete(0, selectHistoryCmd.length());
        return hasPrint;
    }

    private void generateCursorPosPattern() {
        String patternStr = StrUtil.fullFillParam(AsciiControl.ANIS_ESCAPE_CURSOR_LOCATION, prompt.get().length() + 1);
        promptCursorPattern = Pattern.compile(patternStr);
    }

    public String readCmd() throws Exception {
        try {
            shellReader.readCmd();
        } catch (RuntimeException e) {
            return null;
        }

        String cmdStr = cmd.toString();
        boolean isVimCmd = CmdUtil.isViVimCmd(localLastCmd.toString())
                || CmdUtil.isViVimCmd(cmdStr)
                || CmdUtil.isViVimCmd(selectHistoryCmd.toString());
        if (isVimCmd) {
            status = Status.VIM_BEFORE;
        }

        if (CmdUtil.isCdCmd(lastRemoteCmd.toString())
                || CmdUtil.isCdCmd(cmdStr)
                || CmdUtil.isCdCmd(selectHistoryCmd.toString())) {
            StatusCache.EXECUTE_CD_CMD = true;
        }

        boolean isMoreCmd = (CmdUtil.isMoreCmd(localLastCmd.toString()) && !"more".equals(localLastCmd.toString().trim()))
                || (CmdUtil.isMoreCmd(cmdStr) && !"more".equals(cmdStr.trim()))
                || (CmdUtil.isMoreCmd(selectHistoryCmd.toString()) && !"more".equals(selectHistoryCmd.toString().trim()));
        if (isMoreCmd) {
            status = Shell.Status.MORE_BEFORE;
        }

        lastRemoteCmd.delete(0, lastRemoteCmd.length());
        currentPrint.delete(0, currentPrint.length());
        return cmdStr;
    }

    public void extractUserFromPrompt() {
        String preprocess = prompt.get().trim().replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("#", "")
                .trim();
        user = preprocess.split("@")[0];
    }

    public void resize(int width, int height, long sessionId) {
        switch (protocol) {
            case SSH -> {
                ChannelShell channelShell = SshSessionCache.getInstance().getChannelShell(sessionId);
                channelShell.setPtySize(width, height, width, height);
            }
            case MOSH -> {
                MoshSession moshSession = MoshSessionCache.getInstance().get(sessionId);
                moshSession.resize(width, height);
            }
        }
        localLastCmd.delete(0, localLastCmd.length()).append(RESIZE_COMMAND);
    }

    public String fillPrompt(String msg) {
        if (protocol.equals(ShellProtocol.SSH)) {
            return msg;
        }
        Matcher matcher = promptCursorPattern.matcher(msg);
        if (matcher.find()) {
            Matcher doubleCursorMatcher = AsciiControl.ANIS_ESCAPE_DOUBLE_CURSOR_PATTERN.matcher(msg);
            if (doubleCursorMatcher.find()) {
                String changePathStr = doubleCursorMatcher.group(0);
                int[] pos = AsciiControl.extractCursorPosition(changePathStr);
                String setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + AsciiControl.setCursorToLineHead(pos[0]);
                msg = msg.replace(changePathStr, setToHead + prompt.get() + changePathStr);

                int col = pos[1];
                prompt.set(prompt.get().substring(0, col - 1) + AsciiControl.cleanCursorMode(changePathStr) + "]# ");
                generateCursorPosPattern();
                return msg;
            }

            Matcher matcherPrompt = PROMPT_PATTERN.matcher(msg);
            if (!matcherPrompt.find()) {
                String group = matcher.group(0);
                int[] pos = AsciiControl.extractCursorPosition(group);
                String setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + AsciiControl.setCursorToLineHead(pos[0]);
                msg = msg.replace(group, setToHead + prompt.get() + group);
                return msg;
            } else {
                String group = matcher.group(0);
                int[] lastPos = AsciiControl.extractCursorPosition(msg);
                int[] pos = AsciiControl.extractCursorPosition(group);
                if (pos[0] - lastPos[0] == 1) {
                    String setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + AsciiControl.setCursorToLineHead(pos[0]);
                    msg = msg + setToHead + prompt.get();
                    return msg;
                }
            }
        }

        Matcher cursorBracketKMatcher = AsciiControl.ANIS_ESCAPE_CURSOR_BRACKET_K_PATTERN.matcher(msg);
        if (cursorBracketKMatcher.find()) {
            String changePathStr = cursorBracketKMatcher.group(0);
            int[] pos = AsciiControl.extractCursorPosition(changePathStr);
            String setToHead = AsciiControl.ANIS_CLEAR_ALL_MODE + AsciiControl.setCursorToLineHead(pos[0]);
            msg = msg.replace(changePathStr, setToHead + prompt.get() + changePathStr);

            int col = pos[1];
            // there still have problem
            if (col >= prompt.get().length()) {
                return msg;
            }
            prompt.set(prompt.get().substring(0, col - 1) + AsciiControl.cleanCursorMode(changePathStr));
            generateCursorPosPattern();
            return msg;
        }
        return msg;
    }

    public boolean hasWelcome() {
        boolean flag = false;
        switch (protocol) {
            case SSH -> flag = sshWelcome != null;
            case MOSH -> flag = moshWelcome != null;
        }
        return flag;
    }

    public void printWelcome() {
        switch (protocol) {
            case SSH -> Printer.print(sshWelcome.toString());
            case MOSH -> Printer.print(moshWelcome.toString());
        }
    }

    public void printAfterEstablish() {
        Printer.clear();
        if (StatusCache.HANGED_ENTER) {
            Printer.println("Invoke hanged session: " + user + "@" + host);
        } else {
            Printer.println("Session established: " + user + "@" + host);
        }
        Printer.println("\nUse protocol " + protocol.name() + ".\n");
    }

    public void initializeSwitchSessionHelper() {
        this.quickSwitchHelper.initialize();
    }

    public boolean switchSession() {
        return this.quickSwitchHelper.switchSession();
    }

    @SuppressWarnings("all")
    public void initialFirstCorrespondence(ShellProtocol protocol, Executable executable) {
        this.protocol = protocol;
        try {
            if (jumpServer) {
                prompt.set(StrUtil.EMPTY);
                resetIO(protocol);
                executable.execute();
                return;
            }

            CountDownLatch mainLatch = new CountDownLatch(2);

            JsonObject request = new JsonObject();
            request.put("sessionId", sessionId);
            request.put("remotePath", "/" + user + "/.bash_history");
            request.put("type", BlockingDfHandler.DF_TYPE_BYTE);
            if (eventBus != null) {
                eventBus.request(START_DF_COMMAND.address(), request, result -> {
                    if (result == null || result.result() == null) {
                        return;
                    }
                    byte[] bytes = (byte[]) result.result().body();
                    String data = new String(bytes, StandardCharsets.UTF_8);
                    historyCmdHelper.initialize(data.split(StrUtil.LF));
                });
            }

            vertx.executeBlocking(promise -> {
                try {
                    do {
                        if (returnWrite) {
                            return;
                        }
                    } while (!promptNow);

                    if (protocol.equals(ShellProtocol.SSH)) {
                        outputStream.write(StrUtil.LF.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    promise.complete();
                }
            }, false);

            vertx.executeBlocking(promise -> {
                try {
                    byte[] tmp = new byte[1024];
                    long startTime = System.currentTimeMillis();
                    if (channelShell == null) {
                        promptNow = true;
                    }
                    while (true) {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);

                            Matcher matcher = PROMPT_PATTERN.matcher(inputStr);
                            if (matcher.find()) {
                                prompt.set(matcher.group(0).replaceAll("\\[\\?1034h", "") + StrUtil.SPACE);
                                returnWrite = true;
                            } else {
                                if (this.protocol.equals(ShellProtocol.SSH)) {
                                    sshWelcome.append(inputStr);
                                } else if (this.protocol.equals(ShellProtocol.MOSH)) {
                                    moshWelcome.append(inputStr);
                                }
                                returnWrite = true;
                            }
                        }

                        if (System.currentTimeMillis() - startTime >= 1000) {
                            promptNow = true;
                        }

                        if (StringUtils.isNoneEmpty(prompt.get())) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    assert prompt.get() != null;
                    generateCursorPosPattern();
                    extractUserFromPrompt();
                    fullPath.set("/" + user);

                    resetIO(protocol);
                    executable.execute();
                    promise.complete();
                }
            }, false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkConnection() {
        switch (protocol) {
            case SSH -> {
                if (SshSessionCache.getInstance().isDisconnect(sessionId)) {
                    throw new RemoteDisconnectException("SSH session disconnect.");
                }
            }
            case MOSH -> {
                if (MoshSessionCache.getInstance().isDisconnect(sessionId)) {
                    throw new RemoteDisconnectException("Mosh session disconnect.");
                }
            }
            default -> {
            }
        }
    }

    public void flush() {
        checkConnection();
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void write(byte[] bytes) {
        checkConnection();
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void write(char bytes) {
        checkConnection();
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void writeAndFlush(byte[] bytes) {
        checkConnection();
        try {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void writeAndFlush(char inChar) {
        checkConnection();
        try {
            outputStream.write(inChar);
            outputStream.flush();
        } catch (IOException e) {
            throw new RemoteDisconnectException(e.getMessage());
        }
    }

    public void clearShellLineWithPrompt() {
        int promptLen = prompt.get().length();
        int[] position = term.getCursorPosition();
        int cursorX = position[0];
        int cursorY = position[1];
        term.hideCursor();
        term.setCursorPosition(promptLen, cursorY);
        Printer.print(" ".repeat(cursorX - promptLen));
        term.setCursorPosition(promptLen, cursorY);
        term.showCursor();
    }

    public void cleanUp() {
        remoteCmd.delete(0, remoteCmd.length());
        currentPrint.delete(0, currentPrint.length());
        selectHistoryCmd.delete(0, selectHistoryCmd.length());
        localLastCmd.delete(0, localLastCmd.length());
    }

    public void printErr(String err) {
        shellPrinter.printErr(err);
    }

    public boolean isClosed() {
        if (channelShell != null) {
            return channelShell.isClosed();
        }
        if (moshSession != null) {
            return !moshSession.isConnected();
        }
        return true;
    }

    public void setJumpServer(boolean isJumpServer) {
        this.jumpServer = isJumpServer;
    }

    public void clearRemoteCmd() {
        remoteCmd.delete(0, remoteCmd.length());
    }

    public void setLocalLastCmd(String cmd) {
        localLastCmd.delete(0, localLastCmd.length()).append(cmd);
    }

    public Status getStatus() {
        return status;
    }

    public String getSshWelcome() {
        return StringUtils.isEmpty(sshWelcome) ? null : sshWelcome.toString();
    }

    public String getPrompt() {
        return prompt.get();
    }

    public void setPrompt(String prompt) {
        this.prompt.set(prompt);
    }

    public String getLastRemoteCmd() {
        return lastRemoteCmd.toString();
    }

    public String getRemoteCmd() {
        return remoteCmd.toString();
    }

    public String getCurrentPrint() {
        return currentPrint.toString();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFullPath() {
        return fullPath.get();
    }

    public void setFullPath(String fullPath) {
        this.fullPath.set(fullPath);
    }

    public long getSessionId() {
        return sessionId;
    }

    public ChannelShell getChannelShell() {
        return channelShell;
    }

    public void setChannelShell(ChannelShell channelShell) {
        this.channelShell = channelShell;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream(ShellProtocol protocol) {
        try {
            if (protocol.equals(this.protocol)) {
                return outputStream;
            } else if (protocol.equals(ShellProtocol.MOSH)) {
                return moshSession.getOutputStream();
            } else if (protocol.equals(ShellProtocol.SSH)) {
                return channelShell.getOutputStream();
            }
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }

    public InputStream getInputStream(ShellProtocol protocol) {
        try {
            if (protocol.equals(this.protocol)) {
                return inputStream;
            } else if (protocol.equals(ShellProtocol.MOSH)) {
                return moshSession.getInputStream();
            } else if (protocol.equals(ShellProtocol.SSH)) {
                return channelShell.getInputStream();
            }
        } catch (Exception e) {
            // do nothing
        }
        return null;
    }

    public ShellProtocol getProtocol() {
        return this.protocol;
    }

    public String uri() {
        return user + "@" + host;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public enum Status {
        /**
         * The status of Shell.
         */
        NORMAL(1, "Shell is under normal cmd input status."),
        TAB_ACCOMPLISH(2, "Shell is under tab key to auto-accomplish address status."),
        VIM_BEFORE(3, "Shell is before Vim/Vi edit status."),
        VIM_UNDER(4, "Shell is under Vim/Vi edit status."),
        MORE_BEFORE(5, "Shell is before more cmd process status."),
        MORE_PROC(6, "Shell is under more cmd process status."),
        MORE_EDIT(7, "Shell is under more regular expression or cmd edit status."),
        MORE_SUB(8, "Shell is under more :sub cmd status."),
        QUICK_SWITCH(9, "List all connection properties to quick switch session.");

        public final int status;
        public final String comment;

        Status(int status, String comment) {
            this.status = status;
            this.comment = comment;
        }
    }
}
