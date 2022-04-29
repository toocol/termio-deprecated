package com.toocol.ssh.core.cache;

import com.toocol.ssh.core.mosh.core.MoshSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/29 22:24
 * @version: 0.0.1
 */
public class MoshSessionCache {

    private static MoshSessionCache INSTANCE = null;

    private MoshSessionCache() {
    }

    private final Map<Long, MoshSession> moshSessionMap = new HashMap<>();

    public static synchronized MoshSessionCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MoshSessionCache();
        }
        return INSTANCE;
    }

    public void put(MoshSession moshSession) {
        moshSessionMap.put(moshSession.getSessionId(), moshSession);
    }

    public boolean containSession(long sessionId) {
        return moshSessionMap.containsKey(sessionId);
    }

}
