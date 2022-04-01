package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 13:52
 */
public class ShellHangCmdProcessor extends AbstractCommandProcessor {
    @SafeVarargs
    @Override
    public final <T> void process(EventBus eventBus, T... param) throws Exception {
        Future<Long> future = cast(param[0]);
        AtomicBoolean isBreak = cast(param[2]);
        isBreak.set(true);
        future.fail("Hang up the session.");
    }
}
