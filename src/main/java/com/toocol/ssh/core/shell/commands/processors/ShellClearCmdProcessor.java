package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:48
 */
public class ShellClearCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak) {
        Printer.clear();
        return "";
    }
}
