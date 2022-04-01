package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:48
 */
public class ShellClearCmdProcessor extends AbstractCommandProcessor {
    @SafeVarargs
    @Override
    public final <T> void process(EventBus eventBus, T... param) throws Exception {
        Printer.clear();
    }
}
