package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:27
 */
@PreloadDeployment
public class CommandAcceptorVerticle extends AbstractVerticle {

    public static final String ADDRESS_START_ACCEPT = "ssh.command.accept.start";

    public static final String ADDRESS_END_ACCEPT = "ssh.command.accept.end";

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-acceptor-worker");
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_START_ACCEPT, message -> {
            executor.executeBlocking(future -> {
                System.out.println("-- INPUT --");
            }, res -> {
            });
        });
        PrintUtil.println("success start the command acceptor verticle.");
    }
}
