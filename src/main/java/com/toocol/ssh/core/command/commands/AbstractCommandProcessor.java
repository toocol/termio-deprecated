package com.toocol.ssh.core.command.commands;

import com.toocol.ssh.common.utils.ICastable;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:32
 */
public abstract class AbstractCommandProcessor implements ICastable {
    /**
     * process a outside command
     *
     * @param eventBus event bus
     * @param param param
     * @throws Exception e
     */
    @SuppressWarnings("all")
    public abstract <T> void process(EventBus eventBus, T... param) throws Exception;
}
