package com.toocol.ssh.common.handler;

import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.router.IRoutable;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:46
 */
public abstract class AbstractCommandHandler implements IRoutable {
    protected final Vertx vertx;
    /**
     * the event bus of Vert.x
     */
    protected final EventBus eventBus;
    /**
     * the handler's Executor
     */
    protected final WorkerExecutor executor;

    public AbstractCommandHandler(Vertx vertx, WorkerExecutor executor) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        this.executor = executor;
    }

    /**
     * return the address that handler handle of.
     *
     * @return address
     */
    public abstract IAddress address();

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>   generic type
     */
    public abstract <T> void handle(Message<T> message);
}
