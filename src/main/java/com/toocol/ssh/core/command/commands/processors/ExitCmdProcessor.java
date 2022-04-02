package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.OutsideCommandProcessor;
import com.toocol.ssh.common.utils.Tuple;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:53
 */
public class ExitCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple<Boolean, String> tuple) {
        System.exit(-1);
    }
}
