package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.enums.OutsideCommand;
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

    public AcceptOutsideCommandHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_ACCEPT_COMMAND;
    }

    @Override
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(future -> {
            while (true) {
                PrintUtil.printCursorLine();
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine();
                if (OutsideCommand.isOutsideCommand(input)) {
                    eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address(), input);
                    break;
                }
            }
        }, res -> {
        });
    }
}
