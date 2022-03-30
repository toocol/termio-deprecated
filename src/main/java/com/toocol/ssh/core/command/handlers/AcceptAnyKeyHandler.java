package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.util.Scanner;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_ANYKEY;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:12
 */
public class AcceptAnyKeyHandler extends AbstractCommandHandler {

    public AcceptAnyKeyHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_ACCEPT_ANYKEY;
    }

    @Override
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(future -> {
            PrintUtil.printCursorLine();
            Scanner scanner = new Scanner(System.in);
            scanner.next();
        }, res -> {
        });
    }
}
