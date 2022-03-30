package com.toocol.ssh.common.router;

import io.vertx.core.eventbus.EventBus;

/**
 * All the message send to this verticle by Vert.x, then it will distribute the message to correspond next Verticle.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/3/19 16:04
 */
public class MessageRouter {

    public static void route(EventBus eventBus, IAddress currentAddress, Object message) {
        currentAddress.nextAddress().ifPresent(nextAddress -> eventBus.send(nextAddress.address(), message));
    }

}
