package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.Single;
import jline.ConsoleReader;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 20:57
 */
public class Shell {
    private static final Pattern PATTERN = Pattern.compile("(\\[(.*?)])");

    private static ConsoleReader reader = null;

    static {
        try {
            reader = new ConsoleReader(System.in, null);
        } catch (IOException e) {
            Printer.println("Register console reader failed.");
            System.exit(-1);
        }
    }

    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private final OutputStream outputStream;
    private InputStream inputStream;

    private volatile boolean returnWrite = false;
    private volatile boolean promptNow = false;

    public volatile AtomicReference<String> localLastCmd = new AtomicReference<>("");
    protected volatile AtomicReference<String> remoteCmd = new AtomicReference<>("");
    protected volatile AtomicReference<String> currentPrint = new AtomicReference<>("");
    protected volatile String localLastInput = "";
    private Status status = Status.NORMAL;

    private final ShellPrinter shellPrinter = new ShellPrinter(this);
    protected final Set<String> tabFeedbackRec = new HashSet<>();

    private final StringBuffer cmd = new StringBuffer();
    private final Single<String> welcome = new Single<>();
    private final Single<String> prompt = new Single<>();

    @AllArgsConstructor
    public static enum Status {
        /**
         * The status of Shell.
         */
        NORMAL(1, "Shell is under normal cmd input status."),
        TAB_ACCOMPLISH(2, "Shell is under tab key to auto-accomplish address status."),
        VIM(3, "Shell is under Vim/Vi edit status."),
        HANG_UP(4, "Shell is under hang-up status.");

        public final int status;
        public final String comment;
    }

    public Shell(OutputStream outputStream, InputStream inputStream) {
        assert outputStream != null && inputStream != null;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.initialFirstCorrespondence();
    }

    public void print(String msg) {
        if (localLastCmd.get().contains("cd")) {
            Matcher matcher = PATTERN.matcher(msg);
            if (matcher.find()) {
                prompt.valueOf(matcher.group(0) + "# ");
            }
        }

        switch (status) {
            case NORMAL:
                shellPrinter.printInNormal(msg);
                break;
            case TAB_ACCOMPLISH:
                shellPrinter.printInTabAccomplish(msg);
                break;
            default:
                break;
        }
    }

    public String readCmd() throws Exception {
        cmd.delete(0, cmd.length());
        StringBuilder localLastInputBuffer = new StringBuilder();
        while (true) {
            char inChar = (char) reader.readVirtualKey();
            if (inChar == '\t') {
                if (status.equals(Status.TAB_ACCOMPLISH)) {
                    localLastCmd.getAndUpdate(prev -> prev + "\t");
                } else {
                    localLastCmd.set(cmd.append('\t').toString());
                }
                localLastInput = localLastInputBuffer.toString();
                localLastInputBuffer = new StringBuilder();
                cmd.append(inChar);
                outputStream.write(cmd.append('\t').toString().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                cmd.delete(0, cmd.length());
                tabFeedbackRec.clear();
                status = Status.TAB_ACCOMPLISH;
            } else if (inChar == '\b') {
                if (cmd.toString().trim().length() == 0 && status.equals(Status.NORMAL)) {
                    continue;
                }
                if (remoteCmd.get().length() == 0 && status.equals(Status.TAB_ACCOMPLISH)) {
                    continue;
                }
                if (status.equals(Status.TAB_ACCOMPLISH) && remoteCmd.get().length() > 0) {
                    // This is ctrl+backspace
                    cmd.append('\u007F');
                    remoteCmd.getAndUpdate(prev -> remoteCmd.get().substring(0, remoteCmd.get().length() - 1));
                }
                if (status.equals(Status.NORMAL)) {
                    cmd.deleteCharAt(cmd.length() - 1);
                }
                if (localLastInputBuffer.length() > 0) {
                    localLastInputBuffer = new StringBuilder(localLastInputBuffer.substring(0, localLastInputBuffer.length() - 1));
                }
                Printer.print("\b");
                Printer.print(" ");
                Printer.print("\b");
            } else if (inChar == '\r' || inChar == '\n') {
                if (status.equals(Status.TAB_ACCOMPLISH)) {
                    localLastCmd.set(remoteCmd.get() + "\r\n");
                }
                localLastInput = localLastInputBuffer.toString();
                currentPrint.set("");
                Printer.print("\r\n");
                status = Status.NORMAL;
                break;
            } else {
                if (status.equals(Status.TAB_ACCOMPLISH)) {
                    remoteCmd.getAndUpdate(prev -> prev + inChar);
                    localLastCmd.getAndUpdate(prev -> prev + inChar);
                }
                currentPrint.getAndUpdate(prev -> prev + inChar);
                cmd.append(inChar);
                localLastInputBuffer.append(inChar);
                Printer.print(String.valueOf(inChar));
            }
        }
        return cmd.toString();
    }

    public Status getStatus() {
        return status;
    }

    public String getWelcome() {
        return StringUtils.isEmpty(welcome.getValue()) ? null : welcome.getValue();
    }

    public String getPrompt() {
        return prompt.getValue();
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    @SuppressWarnings("all")
    private void initialFirstCorrespondence() {
        try {
            CountDownLatch mainLatch = new CountDownLatch(2);

            new Thread(() -> {
                try {
                    do {
                        if (returnWrite) {
                            return;
                        }
                    } while (!promptNow);

                    outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    Printer.println("writed");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mainLatch.countDown();
                }
            }).start();

            new Thread(() -> {
                try {
                    byte[] tmp = new byte[1024];
                    while (true) {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);

                            if (!inputStr.startsWith("[")) {
                                welcome.valueOf(inputStr);
                                returnWrite = true;
                                break;
                            }

                            if (inputStr.startsWith("\r\n")) {
                                prompt.valueOf(RegExUtils.removeAll("prompt", "\r\n"));
                                returnWrite = true;
                                break;
                            }

                            if (inputStr.startsWith("[")) {
                                prompt.valueOf(inputStr.trim() + " ");
                                returnWrite = true;
                                break;
                            }

                            // Just in case the remote connection deon't send the first prompt information, getting this by ourselves.
                            promptNow = true;
                            if (StringUtils.isNoneEmpty(inputStr)) {
                                prompt.valueOf(inputStr.trim() + " ");
                            }
                        }

                        if (StringUtils.isNoneEmpty(prompt.getValue())) {
                            mainLatch.countDown();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            mainLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assert prompt.getValue() != null;
        // this InputStream is already invalid.
        this.inputStream = null;
    }
}
