package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.enums.OutsideCommand;
import io.netty.util.internal.StringUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.Scanner;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:27
 */
@PreloadDeployment
public class CommandAcceptorVerticle extends AbstractVerticle {

    public static final String ADDRESS_ACCEPT_COMMAND = "ssh.command.accept";

    public static final String ADDRESS_ACCEPT_ANYKEY = "ssh.accept.anykey";

    public static final String ADDRESS_ACCEPT_SELECTION = "ssh.accept.selection";

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-acceptor-worker");
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(ADDRESS_ACCEPT_SELECTION, message -> {
            executor.executeBlocking(future -> {
                while (true) {
                    PrintUtil.printCursorLine();
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    if (!StringUtils.isNumeric(input)) {
                        continue;
                    }
                    if (Integer.parseInt(input) != 1 || Integer.parseInt(input) != 2) {
                       continue;
                    }
                }
            }, res -> {

            });
        });

        eventBus.consumer(ADDRESS_ACCEPT_COMMAND, message -> {
            executor.executeBlocking(future -> {
                while (true) {
                    PrintUtil.printCursorLine();
                    Scanner scanner = new Scanner(System.in);
                    String input = scanner.nextLine();
                    if (OutsideCommand.isOutsideCommand(input)) {
                        eventBus.send(CommandExecutorVerticle.ADDRESS_EXECUTE_OUTSIDE, input);
                        break;
                    }
                }
            }, res -> {
            });
        });

        eventBus.consumer(ADDRESS_ACCEPT_ANYKEY, message -> {
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
