package com.toocol.ssh.core.cache;

import com.toocol.ssh.core.shell.core.Shell;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/29 22:35
 * @version: 0.0.1
 */
public class ShellCache {

    private static ShellCache INSTANCE = null;

    public ShellCache() {
    }

    public synchronized static ShellCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShellCache();
        }
        return INSTANCE;
    }

    /**
     * the map stored all alive ssh session shell's object.
     */
    private final Map<Long, Shell> shellMap = new ConcurrentHashMap<>();

    public boolean contains(long sessionId) {
        return shellMap.containsKey(sessionId);
    }

    public void putShell(long sessionId, Shell shell) {
        shellMap.put(sessionId, shell);
    }

    public Shell getShell(long sessionId) {
        return shellMap.get(sessionId);
    }

    public void stop(long sessionId) {
        shellMap.computeIfPresent(sessionId, (k, v) -> {
            switch (v.getProtocol()) {
                case SSH -> SshSessionCache.getInstance().stop(sessionId);
                case MOSH -> MoshSessionCache.getInstance().stop(sessionId);
            }
            return null;
        });
    }

}
