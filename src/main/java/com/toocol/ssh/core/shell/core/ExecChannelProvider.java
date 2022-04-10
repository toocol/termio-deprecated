package com.toocol.ssh.core.shell.core;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.toocol.ssh.common.utils.ICastable;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.cache.SessionCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:48
 * @version: 0.0.1
 */
public class ExecChannelProvider implements ICastable {

    private static final int MAXIMUM_CACHE_SIZE = 30;
    private static final int EXPIRE_AFTER_ACCESS_AFTER_MINUTES = 10;

    private static final ExecChannelProvider INSTANCE = new ExecChannelProvider();

    private final SessionCache sessionCache = SessionCache.getInstance();

    private final CacheLoader<Long, ChannelExec> cacheLoader = new CacheLoader<>() {
        @Override
        @Nonnull
        public ChannelExec load(@Nullable Long sessionId) throws Exception {
            Session session= sessionCache.getSession(sessionId);
            if (session == null) {
                throw new RuntimeException("Session is null.");
            }
            if (!session.isConnected()) {
                throw new RuntimeException("Session is not connected.");
            }
            ChannelExec channelSftp = cast(session.openChannel("exec"));
            channelSftp.connect();
            return channelSftp;
        }
    };

    private final LoadingCache<Long, ChannelExec> channelExecCache = CacheBuilder.newBuilder()
            .maximumSize(MAXIMUM_CACHE_SIZE)
            .expireAfterAccess(EXPIRE_AFTER_ACCESS_AFTER_MINUTES, TimeUnit.MINUTES)
            .removalListener((RemovalListener<Long, ChannelExec>) removalNotification -> Optional.ofNullable(removalNotification.getValue()).ifPresent(ChannelExec::disconnect))
            .build(cacheLoader);

    private ExecChannelProvider() {
    }

    public static ExecChannelProvider getInstance() {
        return INSTANCE;
    }

    public ChannelExec getChannelExec(Long sessionId) {
        try {
            return channelExecCache.get(sessionId);
        } catch (ExecutionException e) {
            Printer.printErr("Get channel exec failed, message = " + e.getMessage());
        }
        return null;
    }
}
