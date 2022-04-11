package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.START_DF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:34
 * @version: 0.0.1
 */
public class ShellDfCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String cmd) {
        String[] split = cmd.trim().replaceAll(" {2,}"," ").split(" ");
        if (split.length != 2) {
            return "";
        }

        String remotePath = split[1];
        eventBus.send(START_DF_COMMAND.address(), remotePath);

        return "";
    }
}
