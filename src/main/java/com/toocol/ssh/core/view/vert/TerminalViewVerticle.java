package com.toocol.ssh.core.view.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.vert.CommandAcceptorVerticle;
import com.toocol.ssh.core.command.vert.CommandExecutorVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:47
 */
public class TerminalViewVerticle extends AbstractVerticle {

    public static final String ADDRESS_SCREEN_HAS_CLEARED = "ssh.terminal.view";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_SCREEN_HAS_CLEARED, showWitch -> {
            PrintUtil.printPromptScene();
            eventBus.send(CommandAcceptorVerticle.ADDRESS_START_ACCEPT, "start");
            eventBus.send(CommandExecutorVerticle.ADDRESS_EXECUTE, "git-bash:/D/ZhaoZhe/software/Git/git-bash.exe");
        });
        PrintUtil.println("success start the ssh terminal view verticle.");

        WorkerExecutor executor = vertx.createSharedWorkerExecutor("terminal-view-worker");
        executor.executeBlocking(future -> {
            PrintUtil.loading();
            future.complete("loaded");
        }, res -> {
            eventBus.send(CommandExecutorVerticle.ADDRESS_CLEAR, null);
        });
    }
}
