package com.toocol.termio.console.ssh.handlers;

import com.toocol.termio.core.ssh.handlers.AbstractBlockingEstablishSshSessionHandler;
import com.toocol.termio.utilities.functional.Ordered;
import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 11:43
 */
@Ordered
public final class BlockingEstablishSshSessionHandler extends AbstractBlockingEstablishSshSessionHandler {

    public BlockingEstablishSshSessionHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

}
