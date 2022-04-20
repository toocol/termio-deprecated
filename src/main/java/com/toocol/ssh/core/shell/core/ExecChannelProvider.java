package com.toocol.ssh.core.shell.core;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.toocol.ssh.common.utils.ICastable;
import com.toocol.ssh.core.cache.SessionCache;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/10 17:48
 * @version: 0.0.1
 */
public final class ExecChannelProvider implements ICastable {

    private static final ExecChannelProvider INSTANCE = new ExecChannelProvider();

    private final SessionCache sessionCache = SessionCache.getInstance();

    public static ExecChannelProvider getInstance() {
        return INSTANCE;
    }

    public ChannelExec getChannelExec(Long sessionId) throws Exception {
        Session session= sessionCache.getSession(sessionId);
        if (session == null) {
            throw new RuntimeException("Session is null.");
        }
        if (!session.isConnected()) {
            throw new RuntimeException("Session is not connected.");
        }
        return cast(session.openChannel("exec"));
    }

}
