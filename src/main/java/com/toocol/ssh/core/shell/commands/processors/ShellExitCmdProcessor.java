package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:47
 */
public class ShellExitCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Future<Long> future, long sessionId, AtomicBoolean isBreak) {
        isBreak.set(true);
        future.complete(sessionId);
        return "";
    }
}
