package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:53
 */
public class ExitCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> tuple) {
        System.exit(-1);
    }
}
