package com.toocol.ssh.core.cache;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.toocol.ssh.core.ssh.core.SshSession;
import com.toocol.ssh.utilities.functional.Switchable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:12
 */
public class SshSessionCache {

    private static final SshSessionCache INSTANCE = new SshSessionCache();
    /**
     * the map stored all alive ssh session(include Session and ChannelShell)
     */
    private final Map<Long, SshSession> sshSessionMap = new ConcurrentHashMap<>();

    private SshSessionCache() {
    }

    public static SshSessionCache getInstance() {
        return INSTANCE;
    }

    public static int getAlive() {
        return INSTANCE.sshSessionMap.entrySet().stream()
                .filter(entry -> entry.getValue().alive())
                .toList()
                .size();
    }

    public synchronized Map<Long, SshSession> getSessionMap() {
        return sshSessionMap;
    }

    public boolean isAlive(String ip) {
        long sessionId = containSession(ip);
        if (sessionId == 0) {
            return false;
        }
        return sshSessionMap.get(sessionId).alive();
    }

    public long containSession(String ip) {
        return sshSessionMap.entrySet().stream()
                .filter(entry -> ip.equals(entry.getValue().getHost()))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(0L);
    }

    public boolean containSessionId(long sessionId) {
        return sshSessionMap.containsKey(sessionId);
    }

    public boolean isDisconnect(long sessionId) {
        if (!sshSessionMap.containsKey(sessionId)) {
            return false;
        }
        return !sshSessionMap.get(sessionId).alive();
    }

    public void putSshSession(Long sessionId, SshSession session) {
        sshSessionMap.put(sessionId, session);
    }

    public void setSession(Long sessionId, Session session) {
        sshSessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.setSession(session);
            return v;
        });
    }

    public void setChannelShell(Long sessionId, ChannelShell channelShell) {
        sshSessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.setChannelShell(channelShell);
            return v;
        });
    }

    public Session getSession(Long sessionId) {
        return Optional.ofNullable(sshSessionMap.get(sessionId)).map(SshSession::getSession).orElse(null);
    }

    public ChannelShell getChannelShell(Long sessionId) {
        return Optional.ofNullable(sshSessionMap.get(sessionId)).map(SshSession::getChannelShell).orElse(null);
    }

    public synchronized void stopChannelShell(long sessionId) {
        sshSessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.stopChannelShell();
            return v;
        });
    }

    public synchronized void stop(long sessionId) {
        sshSessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.stop();
            return null;
        });
    }

    public synchronized void stop(String host) {
        long sessionId = containSession(host);
        sshSessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.stop();
            return null;
        });
        ShellCache.getInstance().stop(sessionId);
    }

    public synchronized void stopAll() {
        sshSessionMap.forEach((k, v) -> v.stop());
    }

    public Collection<Switchable> getAllSwitchable() {
        return sshSessionMap.values()
                .stream()
                .map(sshSession -> (Switchable) sshSession)
                .toList();
    }
}
