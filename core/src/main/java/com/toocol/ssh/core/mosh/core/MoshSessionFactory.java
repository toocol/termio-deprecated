package com.toocol.ssh.core.mosh.core;

import com.jcraft.jsch.ChannelShell;
import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.cache.ShellCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.ssh.core.SshSessionFactory;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.Vertx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/25 19:47
 */
public class MoshSessionFactory {
    private static MoshSessionFactory FACTORY;

    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final SshSessionFactory sshSessionFactory = SshSessionFactory.factory();

    private final Vertx vertx;

    private MoshSessionFactory(Vertx vertx) {
        this.vertx = vertx;
    }

    public static synchronized MoshSessionFactory factory(Vertx vertx) {
        if (FACTORY == null) {
            FACTORY = new MoshSessionFactory(vertx);
        }
        return FACTORY;
    }

    /**
     * creating udp connection to mosh-server and starting data transport;
     */
    public MoshSession getSession(SshCredential credential) {
        long sessionId = sshSessionCache.containSession(credential.getHost());
        if (sessionId != 0) {
            sessionId = sshSessionFactory.invokeSession(sessionId, credential, null);
        } else {
            sessionId = sshSessionFactory.createSession(credential, null);
        }

        Tuple2<Integer, String> portKey = sshTouch(sessionId);
        if (portKey == null) {
            return null;
        }
        return new MoshSession(vertx, sessionId, credential.getHost(), portKey._1(), portKey._2()).connect();
    }

    /**
     * touch mosh-server by ssh;
     *
     * @return mosh server port / key
     */
    private Tuple2<Integer, String> sshTouch(long sessionId) {

        ChannelShell shell = sshSessionCache.getChannelShell(sessionId);

        Tuple2<Integer, String> portKey = new Tuple2<>();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            InputStream inputStream = shell.getInputStream();
            AtomicBoolean failed = new AtomicBoolean(false);

            new Thread(() -> {
                byte[] tmp = new byte[1024];
                while (true) {
                    try {
                        while (inputStream.available() > 0) {
                            int i = inputStream.read(tmp, 0, 1024);
                            if (i < 0) {
                                break;
                            }
                            String inputStr = new String(tmp, 0, i);

                            for (String line : inputStr.split("\r\n")) {
                                if (line.contains("MOSH CONNECT")) {
                                    String[] split = line.split(" ");
                                    portKey.first(Integer.parseInt(split[2])).second(split[3]);
                                    latch.countDown();
                                }
                            }
                        }
                    } catch (Exception e) {
                        failed.set(true);
                        latch.countDown();
                    }

                }
            }).start();

            new Thread(() -> {
                try {
                    OutputStream outputStream = shell.getOutputStream();
                    outputStream.write("mosh-server\n".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            boolean suc = latch.await(20, TimeUnit.SECONDS);
            if (!suc || failed.get()) {
                return null;
            }
            return portKey;
        } catch (Exception e) {
            return null;
        }
    }

}
