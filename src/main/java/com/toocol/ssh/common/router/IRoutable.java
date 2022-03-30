package com.toocol.ssh.common.router;

import io.vertx.core.eventbus.EventBus;

/**
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:26
 */
public interface IRoutable {

    /**
     * Route the current address to next address to process, base on the MessageRouter.
     *
     * @param eventBus eventBus from Vert.x
     * @param currentAddress current address
     * @param message message
     */
    default void route(EventBus eventBus, IAddress currentAddress, Object message) {
        MessageRouter.route(eventBus, currentAddress, message);
    }

}
