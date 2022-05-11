package com.toocol.ssh.utilities.handler;

import com.toocol.ssh.utilities.utils.ExitMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 10:46
 */
public abstract class AbstractBlockingMessageHandler<R> extends AbstractMessageHandler {
    /**
     * whether the handler is handle parallel
     */
    private final boolean parallel;

    public AbstractBlockingMessageHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context);
        this.parallel = parallel;
    }

    /**
     * handle the message event
     *
     * @param message message event
     * @param <T>     generic type
     */
    public <T> void handle(Message<T> message) {
        context.executeBlocking(
                promise -> {
                    try {
                        handleWithinBlocking(cast(promise), message);
                    } catch (Exception e) {
                        ExitMessage.setMsg("Caught exception, exit program. class=" + this.getClass().getName() + " ,message=" + e.getMessage());
                        System.exit(-1);
                    }
                },
                !parallel,
                asyncResult -> {
                    try {
                        resultWithinBlocking(cast(asyncResult), message);
                    } catch (Exception e) {
                        ExitMessage.setMsg("Caught exception, exit program. class=" + this.getClass().getName() + " ,message=" + e.getMessage());
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
