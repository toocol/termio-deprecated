package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:47
 */
public class ShellExitCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        isBreak.set(true);
        return new Tuple2<>("exit", shell.getSessionId());
    }
}
