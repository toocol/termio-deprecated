package com.toocol.ssh.common.handler;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.utils.ICastable;
import com.toocol.ssh.common.utils.Printer;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:46
 */
public abstract class AbstractMessageHandler<R> implements ICastable {
    /**
     * the vertx system object.
     */
    protected final Vertx vertx;
    /**
     * the event bus of Vert.x
     */
    protected final EventBus eventBus;
    /**
     * the handler's Executor
     */
    protected final WorkerExecutor executor;
    /**
     * whether the handler is handle parallel
     */
    private final boolean parallel;

    public AbstractMessageHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        this.executor = executor;
        this.parallel = parallel;
    }

    /**
     * return the address that handler handle of.
     *
     * @return address
     */
    public abstract IAddress consume();

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>     generic type
     */
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(
                future -> {
                    try {
                        handleWithinBlocking(cast(future), message);
                    } catch (Exception e) {
                        Printer.println("Caught handle exception, exit program. message=" + e.getMessage());
                        System.exit(-1);
                    }
                },
                !parallel,
                asyncResult -> {
                    try {
                        resultWithinBlocking(cast(asyncResult), message);
                    } catch (Exception e) {
                        Printer.println("Caught handle exception, exit program. message=" + e.getMessage());
                        System.exit(-1);
                    }
                }
        );
    }

    /**
     * execute the blocked process
     *
     * @param promise promise
     * @param message message
     * @param <T>     generic type
     * @throws Exception exception
     */
    protected abstract <T> void handleWithinBlocking(Promise<R> promise, Message<T> message) throws Exception;

    /**
     * response the blocked process result
     *
     * @param asyncResult async result
     * @param message     message
     * @param <T>         generic type
     * @throws Exception exception
     */
    protected abstract <T> void resultWithinBlocking(AsyncResult<R> asyncResult, Message<T> message) throws Exception;
}
