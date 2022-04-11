package com.toocol.ssh.core.shell.commands;

import io.vertx.core.Promise;
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
     * @param promise promise
     * @param sessionId session's id
     * @param isBreak break the shell accept cycle
     * @return final cmd should be executed
     */
    public abstract String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String cmd);

}
