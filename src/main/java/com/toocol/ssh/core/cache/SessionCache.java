package com.toocol.ssh.core.cache;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import com.toocol.ssh.core.shell.core.Shell;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:12
 */
public class SessionCache {

    private SessionCache(){}

    private static final SessionCache INSTANCE = new SessionCache();

    public static SessionCache getInstance() {
        return INSTANCE;
    }

    public static int getHangUp() {
        return INSTANCE.channelShellMap.entrySet().stream()
                .filter(entry -> !entry.getValue().isClosed())
                .toList()
                .size();
    }

    /**
     * the map stored all alive ssh session.
     */
    private final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * the map stored all alive ssh channelShell.
     */
    private final Map<Long, ChannelShell> channelShellMap = new ConcurrentHashMap<>();

    /**
     * the map stored all alive ssh session shell's object.
     */
    private final Map<Long, Shell> shellMap = new ConcurrentHashMap<>();

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

    public void putShell(Long sessionId, Shell shell) {
        shellMap.put(sessionId, shell);
    }

    public Shell getShell(Long sessionId) {
        return shellMap.get(sessionId);
    }

    public void stop(long sessionId) {
        sessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.disconnect();
            return null;
        });
        shellMap.computeIfPresent(sessionId, (k, v) -> null);
    }

    public void stopAll() {
        sessionMap.forEach((k, v) -> v.disconnect());
    }

    public long containSession(String ip) {
        return sessionMap.entrySet().stream()
                .filter(entry -> ip.equals(entry.getValue().getHost()))
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(0L);
    }
}
