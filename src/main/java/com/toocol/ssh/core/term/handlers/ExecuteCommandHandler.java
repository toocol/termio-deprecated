package com.toocol.ssh.core.term.handlers;

import com.toocol.ssh.common.handler.AbstractMessageHandler;
import com.toocol.ssh.common.address.IAddress;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.commands.TermioCommand;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.term.TermAddress.ADDRESS_EXECUTE_OUTSIDE;


/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:09
 */
public class ExecuteCommandHandler extends AbstractMessageHandler<Tuple2<Boolean, String>> {

    public ExecuteCommandHandler(Vertx vertx, Context context, boolean parallel) {
        super(vertx, context, parallel);
    }

    @Override
    public IAddress consume() {
        return ADDRESS_EXECUTE_OUTSIDE;
    }

    @Override
    protected <T> void handleWithinBlocking(Promise<Tuple2<Boolean, String>> promise, Message<T> message) {
        String cmd = String.valueOf(message.body());
        Tuple2<Boolean, String> resultAndMessage = new Tuple2<>();
        TermioCommand.cmdOf(cmd)
                .ifPresent(termioCommand -> {
                    try {
                        termioCommand.processCmd(eventBus, cmd, resultAndMessage);
                    } catch (Exception e) {
                        Printer.printErr("Execute command failed, message = " + e.getMessage());
                    }
                });
        promise.complete(resultAndMessage);
    }

    @Override
    protected <T> void resultWithinBlocking(AsyncResult<Tuple2<Boolean, String>> asyncResult, Message<T> message) {
        Tuple2<Boolean, String> resultAndMessage = asyncResult.result();
        if (resultAndMessage._1()) {
            message.reply(null);
        } else {
            message.reply(resultAndMessage._2());
        }
    }
}
