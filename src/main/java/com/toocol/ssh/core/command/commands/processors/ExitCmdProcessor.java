package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:53
 */
public class ExitCmdProcessor extends AbstractCommandProcessor {
    @SafeVarargs
    @Override
    public final <T> void process(EventBus eventBus, T... param) {
        System.exit(-1);
    }
}
