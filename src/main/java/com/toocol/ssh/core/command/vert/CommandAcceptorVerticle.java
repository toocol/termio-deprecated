package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.enums.OutsideCommand;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import java.util.Scanner;

import static com.toocol.ssh.core.command.CommandAcceptorAddress.ADDRESS_ACCEPT_ANYKEY;
import static com.toocol.ssh.core.command.CommandAcceptorAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.command.CommandExecutorAddress.ADDRESS_EXECUTE_OUTSIDE;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:27
 */
@PreloadDeployment
public class CommandAcceptorVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-acceptor-worker");
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(ADDRESS_ACCEPT_COMMAND.address(), message -> {
            executor.executeBlocking(future -> {
                while (true) {
                    PrintUtil.printCursorLine();
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    if (OutsideCommand.isOutsideCommand(input)) {
                        eventBus.send(ADDRESS_EXECUTE_OUTSIDE.address, input);
                        break;
                    }
                }
            }, res -> {
            });
        });

        eventBus.consumer(ADDRESS_ACCEPT_ANYKEY.address(), message -> {
            executor.executeBlocking(future -> {
                PrintUtil.printCursorLine();
                Scanner scanner = new Scanner(System.in);
                scanner.next();
            }, res -> {
            });
        });

        PrintUtil.println("success start the command acceptor verticle.");
    }
}
