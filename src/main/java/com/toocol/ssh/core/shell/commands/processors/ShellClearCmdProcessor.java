package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Printer;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:48
 */
public class ShellClearCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        Printer.clear();
        return new Tuple2<>(null, null);
    }
}
