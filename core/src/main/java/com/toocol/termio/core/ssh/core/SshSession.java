package com.toocol.termio.core.ssh.core;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.toocol.termio.core.cache.ShellCache;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.core.ShellProtocol;
import com.toocol.termio.utilities.functional.Switchable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 15:32
 */
public class SshSession implements Switchable {

    private final long createTime = System.currentTimeMillis();
    private final long sessionId;

    private String host;
    private String user;
    private Session session;
    private ChannelShell channelShell;

    public SshSession(long sessionId) {
        this.sessionId = sessionId;
    }

    public SshSession(long sessionId, Session session, ChannelShell channelShell) {
        this.host = session.getHost();
        this.user = session.getUserName();
        this.sessionId = sessionId;
        this.session = session;
        this.channelShell = channelShell;
    }

    public synchronized void stop() {
        stopChannelShell();
        stopSession();
    }

    public synchronized void stopSession() {
        if (this.session == null) {
            return;
        }
        this.session.disconnect();
        this.session = null;
    }

    public synchronized void stopChannelShell() {
        if (channelShell == null) {
            return;
        }
        this.channelShell.disconnect();
        this.channelShell = null;
    }

    public Session getSession() {
        return session;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getHost() {
        return host;
    }

    public ChannelShell getChannelShell() {
        return channelShell;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setChannelShell(ChannelShell channelShell) {
        this.channelShell = channelShell;
    }

    @Override
    public String uri() {
        return user + "@" + host;
    }

    @Override
    public String protocol() {
        return ShellProtocol.SSH.name();
    }

    @Override
    public String currentPath() {
        return Optional.ofNullable(ShellCache.Instance.getShell(sessionId)).map(shell -> shell.fullPath.get()).orElse("*");
    }

    @Override
    public boolean alive() {
        return session.isConnected() && channelShell.isConnected();
    }

    @Override
    public int weight() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (getClass() != o.getClass() && !(o instanceof Switchable))) return false;
        Switchable that = (Switchable) o;
        return Objects.equals(uri(), that.uri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, user);
    }
}
