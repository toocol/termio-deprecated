package com.toocol.ssh.core.mosh.handlers;

import com.toocol.ssh.core.mosh.core.MoshSession;
import com.toocol.ssh.core.mosh.core.MoshSessionFactory;
import com.toocol.ssh.utilities.address.IAddress;
import com.toocol.ssh.utilities.handler.AbstractBlockingMessageHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.mosh.MoshAddress.ESTABLISH_MOSH_SESSION;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/28 23:44
 * @version: 0.0.1
 */
public class BlockingEstablishMoshSessionHandler extends AbstractBlockingMessageHandler<Long> {

    private final MoshSessionFactory moshSessionFactory = MoshSessionFactory.factory(vertx);

    public BlockingEstablishMoshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Long> promise, Message<T> message) throws Exception {

    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Long> asyncResult, Message<T> message) throws Exception {

    }

    @Override
    public IAddress consume() {
        return ESTABLISH_MOSH_SESSION;
    }
}
