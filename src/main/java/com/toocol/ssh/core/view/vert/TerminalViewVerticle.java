package com.toocol.ssh.core.view.vert;

import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.vert.ClearScreenVerticle;
import com.toocol.ssh.core.command.vert.CommandAcceptorVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:47
 */
@FinalDeployment
public class TerminalViewVerticle extends AbstractVerticle {

    public static final String ADDRESS_SCREEN_HAS_CLEARED = "ssh.terminal.view";

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("terminal-view-worker");
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_SCREEN_HAS_CLEARED, showWitch -> {
            PrintUtil.printPromptScene();
            eventBus.send(CommandAcceptorVerticle.ADDRESS_ACCEPT_COMMAND, "start");
        });
        PrintUtil.println("success start the ssh terminal view verticle.");

        executor.executeBlocking(future -> {
            PrintUtil.loading();
            future.complete("loaded");
        }, res -> eventBus.send(ClearScreenVerticle.ADDRESS_CLEAR, "prompt"));
    }
}
