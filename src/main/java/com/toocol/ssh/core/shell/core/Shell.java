package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.CmdUtil;
import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.eventbus.EventBus;
import lombok.AllArgsConstructor;
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

    protected final ShellPrinter shellPrinter;
    protected final ShellReader shellReader;
    protected final HistoryCmdHelper historyCmdHelper;

    public volatile AtomicReference<String> localLastCmd = new AtomicReference<>(StrUtil.EMPTY);
    public volatile AtomicReference<String> remoteCmd = new AtomicReference<>(StrUtil.EMPTY);
    protected volatile AtomicReference<String> currentPrint = new AtomicReference<>(StrUtil.EMPTY);
    protected volatile AtomicReference<String> selectHistoryCmd = new AtomicReference<>(StrUtil.EMPTY);
    protected volatile String localLastInput = StrUtil.EMPTY;
    protected volatile String lastRemoteCmd = StrUtil.EMPTY;
    protected volatile String lastExecuteCmd =StrUtil.EMPTY;
    protected volatile Status status = Status.NORMAL;

    protected final Set<String> tabFeedbackRec = new HashSet<>();

    protected final StringBuilder cmd = new StringBuilder();
    protected final AtomicReference<String> welcome = new AtomicReference<>();
    protected final AtomicReference<String> prompt = new AtomicReference<>();
    protected final AtomicReference<String> user = new AtomicReference<>();
    protected final AtomicReference<String> fullPath = new AtomicReference<>();

    @AllArgsConstructor
    public enum Status {
        /**
         * The status of Shell.
         */
        NORMAL(1, "Shell is under normal cmd input status."),
        TAB_ACCOMPLISH(2, "Shell is under tab key to auto-accomplish address status."),
        VIM_BEFORE(3, "Shell is before Vim/Vi edit status."),
        VIM_UNDER(4, "Shell is under Vim/Vi edit status."),
        VIM_AFTER(5, "Shell is under Vim/Vi edit status."),
        UP_HISTORY_CMD_SELECT(6, "Shell is under up arrow history command select status."),
        DOWN_HISTORY_CMD_SELECT(6, "Shell is under up arrow history command select status."),
        ;

        public final int status;
        public final String comment;
    }

    public Shell(EventBus eventBus, OutputStream outputStream, InputStream inputStream) {
        this.eventBus = eventBus;
        assert outputStream != null && inputStream != null;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.shellPrinter = new ShellPrinter(this);
        this.shellReader = new ShellReader(this, this.outputStream);
        this.historyCmdHelper = new HistoryCmdHelper();
        this.initialFirstCorrespondence();
    }

    public boolean print(String msg) {
        Matcher matcher = PROMPT_PATTERN.matcher(msg);
        if (matcher.find()) {
            prompt.set(matcher.group(0) + StrUtil.SPACE);
            extractUserFromPrompt();
            if (status.equals(Status.VIM_UNDER)) {
                status = Status.VIM_AFTER;
            }
        }

        boolean hasPrint = false;
        switch (status) {
            case NORMAL, VIM_AFTER -> hasPrint = shellPrinter.printInNormal(msg);
            case TAB_ACCOMPLISH -> shellPrinter.printInTabAccomplish(msg);
            case VIM_BEFORE, VIM_UNDER -> shellPrinter.printInVim(msg);
            case UP_HISTORY_CMD_SELECT, DOWN_HISTORY_CMD_SELECT -> shellPrinter.printSelectHistoryCommand(msg);
            default -> {
            }
        }

        if (status.equals(Status.VIM_AFTER)) {
            Printer.clear();
            Printer.print(prompt.get());
            status = Status.NORMAL;
        }
        return hasPrint;
    }

    public String readCmd() throws Exception {
        try {
            shellReader.readCmd();
        } catch (RuntimeException e) {
            return null;
        }

        String cmdStr = cmd.toString();
        boolean isVimCmd = (StringUtils.isEmpty(cmd) && CmdUtil.isViVimCmd(localLastCmd.get()))
                || CmdUtil.isViVimCmd(cmd.toString())
                || CmdUtil.isViVimCmd(selectHistoryCmd.get());
        if (isVimCmd) {
            status = Status.VIM_BEFORE;
        }

        if (CmdUtil.isCdCmd(lastRemoteCmd)
                || CmdUtil.isCdCmd(cmd.toString())
                || CmdUtil.isCdCmd(selectHistoryCmd.get())) {
            StatusCache.EXECUTE_CD_CMD = true;
        }

        lastRemoteCmd = StrUtil.EMPTY;
        selectHistoryCmd.set(StrUtil.EMPTY);
        currentPrint.set(StrUtil.EMPTY);
        remoteCmd.set(StrUtil.EMPTY);
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
    private void initialFirstCorrespondence() {
        try {
            CountDownLatch mainLatch = new CountDownLatch(2);
            eventBus.request(START_DF_COMMAND.address(), null, result -> {
                // TODO: accomplish HistoryCmdHelper initialize logic
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

    public void cleanUp() {
        remoteCmd.set(StrUtil.EMPTY);
        currentPrint.set(StrUtil.EMPTY);
        selectHistoryCmd.set(StrUtil.EMPTY);
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

    public AtomicReference<String> getFullPath() {
        return fullPath;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public String getLastRemoteCmd() {
        return lastRemoteCmd;
    }

    public void setPrompt(String prompt) {
        this.prompt.set(prompt);
    }

    public void setFullPath(String fullPath) {
        this.fullPath.set(fullPath);
    }

}
