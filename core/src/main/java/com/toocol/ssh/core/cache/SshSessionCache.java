package com.toocol.ssh.core.cache;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:12
 */
public class SshSessionCache {

    private static final SshSessionCache INSTANCE = new SshSessionCache();
    /**
     * the map stored all alive ssh session.
     */
    private final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * the map stored all alive ssh channelShell.
     */
    private final Map<Long, ChannelShell> channelShellMap = new ConcurrentHashMap<>();

    private SshSessionCache() {
    }

    public static SshSessionCache getInstance() {
        return INSTANCE;
    }

    public static int getAlive() {
        return INSTANCE.sessionMap.entrySet().stream()
                .filter(entry -> entry.getValue().isConnected())
                .map(Map.Entry::getKey)
                .filter(sessionId -> {
                    ChannelShell channelShell = INSTANCE.channelShellMap.get(sessionId);
                    if (channelShell == null) {
                        return false;
                    }
                    return channelShell.isConnected();
                })
                .toList()
                .size();
    }

    public Set<ChannelShell> allChannelShell() {
        return new HashSet<>(channelShellMap.values());
    }

    public synchronized Map<Long, Session> getSessionMap() {
        return sessionMap;
    }

    public boolean isAlive(String ip) {
        long sessionId = containSession(ip);
        if (sessionId == 0) {
            return false;
        }
        boolean sessionConnected = sessionMap.get(sessionId).isConnected();
        if (!sessionConnected) {
            stop(sessionId);
            return false;
        } else {
            return channelShellMap.get(sessionId).isConnected();
        }
    }

    public long containSession(String ip) {
        return sessionMap.entrySet().stream()
                .filter(entry -> ip.equals(entry.getValue().getHost()))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(0L);
    }

    public boolean containSessionId(long sessionId) {
        return sessionMap.containsKey(sessionId);
    }

    public boolean isDisconnect(long sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            return false;
        }
        return !sessionMap.get(sessionId).isConnected() || !channelShellMap.get(sessionId).isConnected();
    }

    public void putSession(Long sessionId, Session session) {
        sessionMap.put(sessionId, session);
    }

    public Session getSession(Long sessionId) {
        return sessionMap.get(sessionId);
    }

    public void putChannelShell(Long sessionId, ChannelShell channelShell) {
        channelShellMap.put(sessionId, channelShell);
    }

    public ChannelShell getChannelShell(Long sessionId) {
        return channelShellMap.get(sessionId);
    }

    public synchronized void stopChannelShell(long sessionId) {
        channelShellMap.computeIfPresent(sessionId, (k, v) -> {
            v.disconnect();
            return null;
        });
    }

    public synchronized void stop(long sessionId) {
        channelShellMap.computeIfPresent(sessionId, (k, v) -> {
            v.disconnect();
            return null;
        });
        sessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.disconnect();
            return null;
        });
    }

    public synchronized void stop(String host) {
        long sessionId = containSession(host);
        channelShellMap.computeIfPresent(sessionId, (k, v) -> {
            v.disconnect();
            return null;
        });
        sessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.disconnect();
            return null;
        });
        ShellCache.getInstance().stop(sessionId);
    }

    public synchronized void stopAll() {
        channelShellMap.forEach((k, v) -> v.disconnect());
        sessionMap.forEach((k, v) -> v.disconnect());
    }
}
