package com.toocol.termio.core.ssh.core;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.toocol.termio.core.auth.core.SshCredential;
import com.toocol.termio.core.cache.SshSessionCache;
import com.toocol.termio.core.shell.core.SshUserInfo;
import com.toocol.termio.utilities.log.Loggable;
import com.toocol.termio.utilities.utils.Castable;
import com.toocol.termio.utilities.utils.SnowflakeGuidGenerator;

import java.util.Properties;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:54
 * @version: 0.0.1
 */
public final class SshSessionFactory implements Castable, Loggable {

    private static final SshSessionFactory FACTORY = new SshSessionFactory();
    private final SnowflakeGuidGenerator guidGenerator = SnowflakeGuidGenerator.getInstance();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final JSch jSch = new JSch();

    private SshSessionFactory() {

    }

    public static SshSessionFactory factory() {
        return FACTORY;
    }

    public long createSession(SshCredential credential) throws Exception {
        long sessionId;
        Session session = jSch.getSession(credential.getUser(), credential.getHost(), credential.getPort());
        session.setPassword(credential.getPassword());
        session.setUserInfo(new SshUserInfo());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(10000);
        session.connect();

        sessionId = guidGenerator.nextId();

        ChannelShell channelShell = cast(session.openChannel("shell"));
        channelShell.setPtyType("xterm");
        channelShell.connect();

        SshSession sshSession = new SshSession(sessionId, session, channelShell);
        sshSessionCache.putSshSession(sessionId, sshSession);

        info("Establish ssh session, sessionId = {}, host = {}, user = {}",
                sessionId, credential.getHost(), credential.getUser());
        return sessionId;
    }

    public long invokeSession(long sessionId, SshCredential credential) throws Exception {
        boolean reopenChannelShell = false;
        Session session = sshSessionCache.getSession(sessionId);
        if (!session.isConnected()) {
            try {
                session.connect();
            } catch (Exception e) {
                sessionId = guidGenerator.nextId();
                session = jSch.getSession(credential.getUser(), credential.getHost(), credential.getPort());
                session.setPassword(credential.getPassword());
                session.setUserInfo(new SshUserInfo());
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setTimeout(30000);
                session.connect();

                sshSessionCache.setSession(sessionId, session);
            }
            reopenChannelShell = true;
            warn("Invoke ssh session failed, re-establish ssh session, sessionId = {}, host = {}, user = {}",
                    sessionId, credential.getHost(), credential.getUser());
        } else {
            info("Multiplexing ssh session, sessionId = {}, host = {}, user = {}",
                    sessionId, credential.getHost(), credential.getUser());
        }

        ChannelShell channelShell = sshSessionCache.getChannelShell(sessionId);
        if (reopenChannelShell) {

            sshSessionCache.stopChannelShell(sessionId);
            channelShell = cast(session.openChannel("shell"));
            channelShell.setPtyType("xterm");
            channelShell.connect();
            sshSessionCache.setChannelShell(sessionId, channelShell);

        } else if (channelShell.isClosed() || !channelShell.isConnected()) {

            channelShell = cast(session.openChannel("shell"));
            channelShell.setPtyType("xterm");
            channelShell.connect();
            sshSessionCache.setChannelShell(sessionId, channelShell);

        }

        return sessionId;
    }

}
