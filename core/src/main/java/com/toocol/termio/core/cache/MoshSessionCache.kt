package com.toocol.termio.core.cache;

import com.toocol.termio.core.mosh.core.MoshSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/29 22:24
 * @version: 0.0.1
 */
public class MoshSessionCache {

    private static MoshSessionCache INSTANCE = null;
    private final Map<Long, MoshSession> moshSessionMap = new HashMap<>();

    private MoshSessionCache() {
    }

    public static synchronized MoshSessionCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MoshSessionCache();
        }
        return INSTANCE;
    }

    public Map<Long, MoshSession> getSessionMap() {
        return moshSessionMap;
    }

    public void put(MoshSession moshSession) {
        moshSessionMap.put(moshSession.getSessionId(), moshSession);
    }

    public MoshSession get(long sessionId) {
        return moshSessionMap.get(sessionId);
    }

    public boolean containSession(long sessionId) {
        return moshSessionMap.containsKey(sessionId);
    }

    public boolean isDisconnect(long sessionId) {
        if (!containSession(sessionId)) {
            return true;
        }
        return !moshSessionMap.get(sessionId).isConnected();
    }

    public void stop(long sessionId) {
        moshSessionMap.computeIfPresent(sessionId, (k, v) -> {
            v.close();
            return null;
        });
    }

    public void stopAll() {
        moshSessionMap.forEach((aLong, moshSession) -> moshSession.close());
    }
}
