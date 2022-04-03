package com.toocol.ssh.core.shell.core;

import com.toocol.ssh.common.utils.Tuple;
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
    /**
     * the output/input Stream belong to JSch's channelShell;
     */
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final Tuple<String> welcome = new Tuple<>();
    private final Tuple<String> prompt = new Tuple<>();

    private volatile boolean returnNow = false;
    private volatile boolean executeNow = false;

    private String localCmd;
    private String remoteCmd;
    private Status status;

    @AllArgsConstructor
    private enum Status {
        NORMAL(1, "Shell is under normal cmd input status."),
        TAB_ACCOMPLISH(2, "Shell is under tab key to auto-accomplish address status."),
        VIM(3, "Shell is under Vim/Vi edit status.")
        ;
        public final int status;
        public final String comment;
    }

    public Shell(OutputStream outputStream, InputStream inputStream) {
        assert outputStream != null && inputStream != null;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.getPromptFromRemote();

    }

    public void print(String msg) {

    }

    public String readCmd() {
        return null;
    }

    public String getWelcome() {
        return StringUtils.isEmpty(welcome._1()) ? null : welcome._1();
    }

    public String getPrompt() {
        return prompt._1();
    }

    private void getPromptFromRemote() {
        try {
            CountDownLatch mainLatch = new CountDownLatch(2);

            new Thread(() -> {
                try {
                    do {
                        if (returnNow) {
                            return;
                        }
                    } while (!executeNow);

                    outputStream.write("\n".getBytes(StandardCharsets.UTF_8));
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
                    while (true) {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);
                            if (!inputStr.startsWith("[")) {
                                welcome.first(inputStr);
                                returnNow = true;
                                break;
                            }
                            if (inputStr.startsWith("\r\n")) {
                                this.prompt.first(RegExUtils.removeAll("prompt", "\r\n"));
                                returnNow = true;
                                break;
                            }
                            executeNow = true;
                            prompt.first(inputStr.trim() + " ");
                        }

                        if (StringUtils.isNoneEmpty(prompt._1())) {
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
        assert prompt._1() != null;
    }
}
