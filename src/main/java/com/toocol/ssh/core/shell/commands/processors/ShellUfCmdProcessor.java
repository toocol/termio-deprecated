package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.shell.ShellAddress.START_UF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:33
 * @version: 0.0.1
 */
public class ShellUfCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String cmd) {
        Shell shell = SessionCache.getInstance().getShell(sessionId);
        String remotePath = shell.getFullPath().get();

        JsonObject request = new JsonObject();
        request.put("sessionId", sessionId);
        request.put("remotePath", remotePath);

        eventBus.send(START_UF_COMMAND.address(), request);
        return EMPTY;
    }
}
