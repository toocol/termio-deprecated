package com.toocol.termio.core.mosh.core;

import com.jcraft.jsch.ChannelShell;
import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.MoshSessionCache;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.ssh.core.SshSessionFactory;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.Vertx;

import javax.annotation.Nullable;
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
public final class MoshSessionFactory implements Loggable {
    private static MoshSessionFactory FACTORY;

    private final MoshSessionCache moshSessionCache = MoshSessionCache.getInstance();
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
    @Nullable
    public MoshSession getSession(SshCredential credential) {
        try {
            long sessionId = sshSessionCache.containSession(credential.getHost());
            if (sessionId != 0) {
                sessionId = sshSessionFactory.invokeSession(sessionId, credential);
            } else {
                sessionId = sshSessionFactory.createSession(credential);
            }

            Tuple2<Integer, String> portKey = sshTouch(sessionId);
            if (portKey == null) {
                return null;
            }
            MoshSession moshSession = new MoshSession(vertx, sessionId, credential.getHost(), credential.getUser(), portKey._1(), portKey._2());
            moshSessionCache.put(moshSession);
            info("Create mosh session, key = {}, sessionId = {}, host = {}, user = {}",
                    portKey._2(), sessionId, credential.getHost(), credential.getUser());
            return moshSession;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * touch mosh-server by ssh;
     *
     * @return mosh server port / key
     */
    @Nullable
    private Tuple2<Integer, String> sshTouch(long sessionId) {

        ChannelShell shell = sshSessionCache.getChannelShell(sessionId);

        Tuple2<Integer, String> portKey = new Tuple2<>();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            InputStream inputStream = shell.getInputStream();
            AtomicBoolean failed = new AtomicBoolean(false);

            vertx.executeBlocking(promise -> {
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
                                    promise.tryComplete();
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        failed.set(true);
                        latch.countDown();
                        promise.tryComplete();
                    }
                }
            }, false);

            vertx.executeBlocking(promise -> {
                try {
                    OutputStream outputStream = shell.getOutputStream();
                    outputStream.write("export HISTCONTROL=ignoreboth\nmosh-server\n".getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    promise.complete();
                }
            }, false);

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
