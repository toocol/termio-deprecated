package com.toocol.ssh.core.shell.commands;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:42
 */
public abstract class ShellCommandProcessor {

    /**
     * process the shell command
     *
     * @param eventBus event bus
     * @param future future
     * @param sessionId session's id
     * @param isBreak break the shell accept cycle
     * @return final cmd should be execute
     */
    public abstract String process(EventBus eventBus, Future<Long> future, long sessionId, AtomicBoolean isBreak);

}
