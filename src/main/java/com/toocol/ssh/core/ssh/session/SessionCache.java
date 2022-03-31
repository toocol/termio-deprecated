package com.toocol.ssh.core.ssh.session;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:12
 */
public class SessionCache {

    private final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    private final Map<Long, ChannelShell> channelShellMap = new ConcurrentHashMap<>();

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

}
