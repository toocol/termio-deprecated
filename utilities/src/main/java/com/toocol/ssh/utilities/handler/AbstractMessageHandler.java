package com.toocol.ssh.utilities.handler;

import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.execeptions.IStacktraceParser;
import com.toocol.ssh.utilities.log.Loggable;
import com.toocol.ssh.utilities.utils.Castable;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/21 10:34
 */
public abstract class AbstractMessageHandler implements Castable, Loggable, IStacktraceParser {
    /**
     * the vertx system object.
     */
    protected final Vertx vertx;
    /**
     * the context of verticle.
     */
    protected final Context context;
    /**
     * the event bus of Vert.x
     */
    protected final EventBus eventBus;

    protected AbstractMessageHandler(Vertx vertx, Context context) {
        this.vertx = vertx;
        this.context = context;
        this.eventBus = vertx.eventBus();
    }

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>     generic type
     */
    public abstract <T> void handle(Message<T> message);

    /**
     * return the address that handler handle of.
     *
     * @return address
     */
    public abstract IAddress consume();

}
