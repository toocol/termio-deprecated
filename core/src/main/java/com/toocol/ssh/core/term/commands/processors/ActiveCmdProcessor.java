package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.utilities.utils.Tuple2;
import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:45
 * @version: 0.0.1
 */
public final class ActiveCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> tuple) {
        // TODO: see com.toocol.ssh.core.term.commands.processors.NumberCmdProcessor
    }
}
