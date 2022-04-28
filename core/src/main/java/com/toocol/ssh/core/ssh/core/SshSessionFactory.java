package com.toocol.ssh.core.ssh.core;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.core.SshUserInfo;
import com.toocol.ssh.utilities.utils.ICastable;
import com.toocol.ssh.utilities.utils.SnowflakeGuidGenerator;
import io.vertx.core.eventbus.EventBus;

import java.util.Properties;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:54
 * @version: 0.0.1
 */
public final class SshSessionFactory implements ICastable {

    private static final SshSessionFactory FACTORY = new SshSessionFactory();
    public static SshSessionFactory factory() {
        return FACTORY;
    }
    private SshSessionFactory() {

    }

    private final SnowflakeGuidGenerator guidGenerator = new SnowflakeGuidGenerator();
    private final SshSessionCache sshSessionCache = SshSessionCache.getInstance();
    private final JSch jSch = new JSch();

    public long createSession(SshCredential credential, EventBus eventBus) {
        long sessionId;
        try {
            Session session = jSch.getSession(credential.getUser(), credential.getHost(), credential.getPort());
            session.setPassword(credential.getPassword());
            session.setUserInfo(new SshUserInfo());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setTimeout(10000);
            session.connect();

            sessionId = guidGenerator.nextId();
            sshSessionCache.putSession(sessionId, session);

            ChannelShell channelShell = cast(session.openChannel("shell"));
            channelShell.setPtyType("xterm");
            channelShell.connect();
            sshSessionCache.putChannelShell(sessionId, channelShell);

            Shell shell = new Shell(sessionId, eventBus, channelShell.getOutputStream(), channelShell.getInputStream());
            shell.setUser(credential.getUser());
            shell.initialFirstCorrespondence();
            sshSessionCache.putShell(sessionId, shell);
        } catch (Exception e) {
            sessionId = -1;
        }
        return sessionId;
    }

    public long invokeSession(long sessionId, SshCredential credential, EventBus eventBus) {
        try {
            boolean reopenChannelShell = false;
            boolean regenerateShell = false;
            Session session = sshSessionCache.getSession(sessionId);
            if (!session.isConnected()) {
                try {
                    session.connect();
                } catch (Exception e) {
                    session = jSch.getSession(credential.getUser(), credential.getHost(), credential.getPort());
                    session.setPassword(credential.getPassword());
                    session.setUserInfo(new SshUserInfo());
                    Properties config = new Properties();
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config);
                    session.setTimeout(30000);
                    session.connect();

                    sshSessionCache.putSession(sessionId, session);
                }
                reopenChannelShell = true;
                regenerateShell = true;
            }

            ChannelShell channelShell = sshSessionCache.getChannelShell(sessionId);
            if (reopenChannelShell) {

                sshSessionCache.stopChannelShell(sessionId);
                channelShell = cast(session.openChannel("shell"));
                channelShell.setPtyType("xterm");
                channelShell.connect();
                sshSessionCache.putChannelShell(sessionId, channelShell);

            } else if (channelShell.isClosed() || !channelShell.isConnected()) {

                channelShell = cast(session.openChannel("shell"));
                channelShell.setPtyType("xterm");
                channelShell.connect();
                sshSessionCache.putChannelShell(sessionId, channelShell);
                regenerateShell = true;

            }

            if (regenerateShell) {
                Shell shell = new Shell(sessionId, eventBus, channelShell.getOutputStream(), channelShell.getInputStream());
                shell.setUser(credential.getUser());
                shell.initialFirstCorrespondence();
                sshSessionCache.putShell(sessionId, shell);
            }
        } catch (Exception e) {
            sessionId = -1;
        }

        return sessionId;
    }

}
