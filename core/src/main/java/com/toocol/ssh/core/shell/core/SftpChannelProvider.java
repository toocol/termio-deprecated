package com.toocol.ssh.core.shell.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.toocol.ssh.utilities.utils.ICastable;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.cache.SessionCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:26
 * @version: 0.0.1
 */
public final class SftpChannelProvider implements ICastable {

    private static final int MAXIMUM_CACHE_SIZE = 30;
    private static final int EXPIRE_AFTER_ACCESS_AFTER_MINUTES = 10;

    private static final SftpChannelProvider INSTANCE = new SftpChannelProvider();

    private final SessionCache sessionCache = SessionCache.getInstance();

    private final CacheLoader<Long, ChannelSftp> cacheLoader = new CacheLoader<>() {
        @Override
        @Nonnull
        public ChannelSftp load(@Nullable Long sessionId) throws Exception {
            Session session= sessionCache.getSession(sessionId);
            if (session == null) {
                throw new RuntimeException("Session is null.");
            }
            if (!session.isConnected()) {
                throw new RuntimeException("Session is not connected.");
            }
            ChannelSftp channelSftp = cast(session.openChannel("sftp"));
            channelSftp.connect();
            return channelSftp;
        }
    };

    private final LoadingCache<Long, ChannelSftp> channelSftpCache = CacheBuilder.newBuilder()
            .maximumSize(MAXIMUM_CACHE_SIZE)
            .expireAfterAccess(EXPIRE_AFTER_ACCESS_AFTER_MINUTES, TimeUnit.MINUTES)
            .removalListener((RemovalListener<Long, ChannelSftp>) removalNotification -> Optional.ofNullable(removalNotification.getValue()).ifPresent(ChannelSftp::disconnect))
            .build(cacheLoader);

    private SftpChannelProvider() {
    }

    public static SftpChannelProvider getInstance() {
        return INSTANCE;
    }


    public ChannelSftp getChannelSftp(Long sessionId) {
        try {
            return channelSftpCache.get(sessionId);
        } catch (ExecutionException e) {
            Printer.printErr("Get channel exec failed, message = " + e.getMessage());
        }
        return null;
    }

}
