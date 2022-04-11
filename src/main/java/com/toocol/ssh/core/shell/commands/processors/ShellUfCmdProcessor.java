package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.START_UF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:33
 * @version: 0.0.1
 */
public class ShellUfCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String cmd) {
        eventBus.send(START_UF_COMMAND.address(), sessionId);
        return null;
    }
}
