package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 13:52
 */
public class ShellHangCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String cmd) {
        SessionCache.getInstance().getShell(sessionId).cleanUp();
        isBreak.set(true);
        promise.fail("Hang up the session.");
        return EMPTY;
    }
}
