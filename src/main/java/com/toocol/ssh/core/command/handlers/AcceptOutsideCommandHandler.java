package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.commands.OutsideCommand;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import java.util.Scanner;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_OUTSIDE;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:11
 */
public class AcceptOutsideCommandHandler extends AbstractCommandHandler {

    public AcceptOutsideCommandHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_ACCEPT_COMMAND;
    }

    @Override
    protected <R, T> void handleWithin(Future<R> future, Message<T> message) {
        while (true) {
            try {
                PrintUtil.printCursorLine();
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                if (OutsideCommand.isOutsideCommand(input)) {
                    eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), input);
                    PrintUtil.clear();
                }
            } catch (Exception e) {
                PrintUtil.printErr("Application run failed, now exit.");
                System.exit(-1);
            }
        }
    }

    @Override
    protected <R, T> void resultWithin(AsyncResult<R> asyncResult, Message<T> message) {

    }
}
