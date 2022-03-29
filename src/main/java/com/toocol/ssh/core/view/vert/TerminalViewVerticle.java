package com.toocol.ssh.core.view.vert;

import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import static com.toocol.ssh.core.command.ClearScreenAddress.ADDRESS_CLEAR;
import static com.toocol.ssh.core.command.CommandAcceptorAddress.ADDRESS_ACCEPT_COMMAND;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_LOADING;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_SCREEN_HAS_CLEARED;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:47
 */
@FinalDeployment
public class TerminalViewVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("terminal-view-worker");
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(ADDRESS_LOADING.address(), message -> {
            executor.executeBlocking(future -> {
                PrintUtil.loading();
                future.complete("loaded");
            }, res -> eventBus.send(ADDRESS_CLEAR.address(), "start"));
        });

        eventBus.consumer(ADDRESS_SCREEN_HAS_CLEARED.address(), showWitch -> {
            PrintUtil.printPromptScene();
            eventBus.send(ADDRESS_ACCEPT_COMMAND.address(), "start");
        });

        PrintUtil.println("success start the ssh terminal view verticle.");
        eventBus.send(ADDRESS_LOADING.address(), "start");
    }
}
