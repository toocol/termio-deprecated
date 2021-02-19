package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.TerminalSystem;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:27
 */
public class CommandAcceptorVerticle extends AbstractVerticle {

    public static final String ADDRESS_START_ACCEPT = "ssh.command.accept.start";

    public static final String ADDRESS_END_ACCEPT = "ssh.command.accept.end";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_START_ACCEPT, message -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-acceptor-worker");
            executor.executeBlocking(future -> {
                System.out.println("INPUT : Begin to listen keyboard");
            }, res -> {

            });
        });
        PrintUtil.println("success start the command acceptor verticle.");
        TerminalSystem.INITIAL_LATCH.countDown();
    }
}
