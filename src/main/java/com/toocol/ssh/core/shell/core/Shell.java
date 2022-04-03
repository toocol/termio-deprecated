package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.Single;
import com.toocol.ssh.core.cache.Cache;
import jline.ConsoleReader;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/3 20:57
 */
public class Shell {
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
    private final InputStream inputStream;
    private final Single<String> welcome = new Single<>();
    private final Single<String> prompt = new Single<>();

    private final StringBuffer cmd = new StringBuffer();

    private volatile boolean returnWrite = false;
    private volatile boolean promptNow = false;

    private String localCmd;
    private String remoteCmd;
    private Status status;

    @AllArgsConstructor
    private enum Status {
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
        this.status = Status.NORMAL;
        this.initialFirstCorrespondence();

    }

    public void print(String msg) {
        if (Cache.CURRENT_COMMAND.equals(msg)) {
            return;
        } else if (Cache.CURRENT_COMMAND.contains("\t")) {
            if (msg.startsWith("ect/\u0007")) {
                // remove system prompt voice
                msg = msg.split("\u0007")[1];
            }
            if (Cache.CURRENT_COMMAND.startsWith(msg)) {
                return;
            }
            String[] split = msg.split("\r\n");
            if (split.length != 0) {
                msg = "\r\n" + split[split.length - 1];
            }
        } else if (msg.startsWith(Cache.CURRENT_COMMAND)) {
            // cd command's echo is like this: cd /\r\n[host@user address]
            msg = msg.substring(Cache.CURRENT_COMMAND.length());
        }
        Printer.print(msg);
    }

    public String readCmd() throws Exception {
        cmd.delete(0, cmd.length());
        while (true) {
            char inChar = (char) reader.readVirtualKey();
            if (inChar == '\t') {
                Cache.CURRENT_COMMAND = cmd.append('\t').toString();
                cmd.append(inChar);
                outputStream.write(cmd.append('\t').toString().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                cmd.delete(0, cmd.length());
            } else if (inChar == '\b') {
                if (cmd.toString().trim().length() == 0) {
                    continue;
                }
                Printer.print("\b");
                Printer.print(" ");
                Printer.print("\b");
                cmd.deleteCharAt(cmd.length() - 1);
            } else if (inChar == '\r' || inChar == '\n') {
                Printer.print("\r\n");
                break;
            } else {
                Printer.print(String.valueOf(inChar));
                cmd.append(inChar);
            }
        }
        return cmd.toString();
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

    public InputStream getInputStream() {
        return inputStream;
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
                                this.prompt.valueOf(RegExUtils.removeAll("prompt", "\r\n"));
                                returnWrite = true;
                                break;
                            }

                            if (inputStr.startsWith("[")){
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
    }
}
