package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.CmdUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.handlers.DfHandler;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
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
public class Shell {

    public static final Pattern PROMPT_PATTERN = Pattern.compile("(\\[(\\w*?)@(.*?)]#)");

    private ConsoleReader reader;
    {
        try {
            reader = new ConsoleReader(System.in, null, null);
        } catch (Exception e) {
            Printer.println("\nCreate console reader failed.");
            System.exit(-1);
        }
    }

    private final long sessionId;
    /**
     * the EventBus of vert.x system.
     */
    private final EventBus eventBus;
    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private final OutputStream outputStream;
    private InputStream inputStream;

    private volatile boolean returnWrite = false;
    private volatile boolean promptNow = false;

    protected final Term term = Term.getInstance();
    protected final ShellPrinter shellPrinter;
    protected final ShellReader shellReader;
    protected final HistoryCmdHelper historyCmdHelper;
    protected final MoreHelper moreHelper;

    public volatile AtomicReference<StringBuffer> localLastCmd = new AtomicReference<>(new StringBuffer());
    public volatile AtomicReference<StringBuffer> remoteCmd = new AtomicReference<>(new StringBuffer());
    protected volatile AtomicReference<StringBuffer> currentPrint = new AtomicReference<>(new StringBuffer());
    protected volatile AtomicReference<StringBuffer> selectHistoryCmd = new AtomicReference<>(new StringBuffer());
    protected volatile StringBuffer localLastInput = new StringBuffer();
    protected volatile StringBuffer lastRemoteCmd = new StringBuffer();
    protected volatile StringBuffer lastExecuteCmd = new StringBuffer();
    protected volatile Status status = Status.NORMAL;

    protected final Set<String> tabFeedbackRec = new HashSet<>();

    protected final StringBuilder cmd = new StringBuilder();
    protected final AtomicReference<String> welcome = new AtomicReference<>();
    protected final AtomicReference<String> prompt = new AtomicReference<>();
    protected final AtomicReference<String> user = new AtomicReference<>();
    protected final AtomicReference<String> fullPath = new AtomicReference<>();

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
        ;

        Status(int status, String comment) {
            this.status = status;
            this.comment = comment;
        }

        public final int status;
        public final String comment;
    }

    public Shell(long sessionId, EventBus eventBus, OutputStream outputStream, InputStream inputStream) {
        this.sessionId = sessionId;
        this.eventBus = eventBus;
        assert outputStream != null && inputStream != null;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.shellPrinter = new ShellPrinter(this);
        this.shellReader = new ShellReader(this, this.outputStream, reader);
        this.historyCmdHelper = new HistoryCmdHelper(this);
        this.moreHelper = new MoreHelper();

        this.shellReader.addTrigger();
    }

    public boolean print(String msg) {
        Matcher matcher = PROMPT_PATTERN.matcher(msg.trim());
        if (matcher.find()) {
            prompt.set(matcher.group(0) + StrUtil.SPACE);
            extractUserFromPrompt();
            if (status.equals(Status.VIM_UNDER)) {
                status = Status.NORMAL;
            } else if (status.equals(Status.MORE_PROC) || status.equals(Status.MORE_EDIT) || status.equals(Status.MORE_SUB)) {
                status = Status.NORMAL;
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

        if (status.equals(Shell.Status.VIM_BEFORE)) {
            status = Shell.Status.VIM_UNDER;
        }

        selectHistoryCmd.getAndUpdate(prev -> prev.delete(0, prev.length()));
        localLastCmd.getAndUpdate(prev -> prev.delete(0, prev.length()));
        return hasPrint;
    }

    public String readCmd() throws Exception {
        try {
            shellReader.readCmd();
        } catch (RuntimeException e) {
            return null;
        }

        String cmdStr = cmd.toString();
        boolean isVimCmd = CmdUtil.isViVimCmd(localLastCmd.get().toString())
                || CmdUtil.isViVimCmd(cmd.toString())
                || CmdUtil.isViVimCmd(selectHistoryCmd.get().toString());
        if (isVimCmd) {
            status = Status.VIM_BEFORE;
        }

        if (CmdUtil.isCdCmd(lastRemoteCmd.toString())
                || CmdUtil.isCdCmd(cmd.toString())
                || CmdUtil.isCdCmd(selectHistoryCmd.get().toString())) {
            StatusCache.EXECUTE_CD_CMD = true;
        }

        boolean isMoreCmd = CmdUtil.isMoreCmd(localLastCmd.get().toString())
                || CmdUtil.isMoreCmd(cmd.toString())
                || CmdUtil.isMoreCmd(selectHistoryCmd.get().toString());
        if (isMoreCmd) {
            status = Shell.Status.MORE_BEFORE;
        }

        lastRemoteCmd.delete(0, lastRemoteCmd.length());
        currentPrint.getAndUpdate(prev -> prev.delete(0, prev.length()));
        return cmdStr;
    }

    public void extractUserFromPrompt() {
        String preprocess = prompt.get().trim().replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll("#", "")
                .trim();
        user.set(preprocess.split("@")[0]);
    }

    @SuppressWarnings("all")
    public void initialFirstCorrespondence() {
        try {
            CountDownLatch mainLatch = new CountDownLatch(2);

            JsonObject request = new JsonObject();
            request.put("sessionId", sessionId);
            request.put("remotePath", "/" + user + "/.bash_history");
            request.put("type", DfHandler.DF_TYPE_BYTE);
            eventBus.request(START_DF_COMMAND.address(), request, result -> {
                byte[] bytes = (byte[]) result.result().body();
                String data = new String(bytes, StandardCharsets.UTF_8);
                historyCmdHelper.initialize(data.split(StrUtil.LF));
            });

            new Thread(() -> {
                try {
                    do {
                        if (returnWrite) {
                            return;
                        }
                    } while (!promptNow);

                    outputStream.write(StrUtil.LF.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mainLatch.countDown();
                }
            }).start();

            new Thread(() -> {
                try {
                    byte[] tmp = new byte[1024];
                    long startTime = System.currentTimeMillis();
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
                                break;
                            } else {
                                welcome.set(inputStr);
                                returnWrite = true;
                                break;
                            }
                        }

                        if (System.currentTimeMillis() - startTime >= 1000) {
                            promptNow = true;
                        }

                        if (StringUtils.isNoneEmpty(prompt.get())) {
                            mainLatch.countDown();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            mainLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert prompt.get() != null;
        extractUserFromPrompt();
        fullPath.set("/" + user.get());
        // this InputStream is already invalid.
        this.inputStream = null;
    }

    public void clearShellLineWithPrompt() {
        int promptLen = prompt.get().length();
        Tuple2<Integer, Integer> position = term.getCursorPosition();
        int cursorX = position._1();
        int cursorY = position._2();
        term.hideCursor();
        term.setCursorPosition(promptLen, cursorY);
        for (int idx = 0; idx < cursorX - promptLen; idx++) {
            Printer.print(" ");
        }
        term.setCursorPosition(promptLen, cursorY);
        term.showCursor();
    }

    public void cleanUp() {
        remoteCmd.set(new StringBuffer());
        currentPrint.set(new StringBuffer());
        selectHistoryCmd.set(new StringBuffer());
        localLastCmd.set(new StringBuffer());
    }

    public void printErr(String err) {
        shellPrinter.printErr(err);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWelcome() {
        return StringUtils.isEmpty(welcome.get()) ? null : welcome.get();
    }

    public String getPrompt() {
        return prompt.get();
    }

    public AtomicReference<String> getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user.set(user);
    }

    public AtomicReference<String> getFullPath() {
        return fullPath;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getLastRemoteCmd() {
        return lastRemoteCmd.toString();
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setPrompt(String prompt) {
        this.prompt.set(prompt);
    }

    public void setFullPath(String fullPath) {
        this.fullPath.set(fullPath);
    }

}
