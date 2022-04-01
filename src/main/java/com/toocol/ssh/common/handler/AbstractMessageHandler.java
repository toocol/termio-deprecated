package com.toocol.ssh.common.handler;

import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.common.utils.ICastable;
import com.toocol.ssh.common.utils.Printer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
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
                        handleWithin(cast(future), message);
                    } catch (Exception e) {
                        Printer.printErr("\nCaught handle exception, exit program.");
                        System.exit(-1);
                    }
                },
                !parallel,
                asyncResult -> {
                    try {
                        resultWithin(cast(asyncResult), message);
                    } catch (Exception e) {
                        Printer.println("\nCaught handle exception, exit program.");
                        System.exit(-1);
                    }
                }
        );
    }

    /**
     * To inject some extra obj inject,
     * if needed, override this method.
     *
     * @param objs objs to inject
     * @param <T>  generic type
     */
    public <T> void inject(T... objs) {
    }

    /**
     * execute the blocked process
     *
     * @param future  future
     * @param message message
     * @param <T>     generic type
     * @throws Exception exception
     */
    protected abstract <T> void handleWithin(Future<R> future, Message<T> message) throws Exception;

    /**
     * response the blocked process result
     *
     * @param asyncResult async result
     * @param message     message
     * @param <T>         generic type
     * @throws Exception exception
     */
    protected abstract <T> void resultWithin(AsyncResult<R> asyncResult, Message<T> message) throws Exception;
}
