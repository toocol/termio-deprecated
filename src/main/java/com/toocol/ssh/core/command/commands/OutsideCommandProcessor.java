package com.toocol.ssh.core.command.commands;

import com.toocol.ssh.common.utils.Tuple;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:32
 */
public abstract class OutsideCommandProcessor {

    /**
     * process command
     *
     * @param eventBus event bus
     * @param cmd cmd
     * @param tuple tuple
     */
    public abstract void process(EventBus eventBus, String cmd, Tuple<Boolean, String> tuple);

}
