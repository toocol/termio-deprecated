package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:47
 */
public class ShellExitCmdProcessor extends AbstractCommandProcessor {
    @SafeVarargs
    @Override
    public final <T> void process(EventBus eventBus, T... param) throws Exception {
        Future<Long> future = cast(param[0]);
        long sessionId = cast(param[1]);
        AtomicBoolean isBreak = cast(param[2]);

        isBreak.set(true);
        future.complete(sessionId);
    }
}
