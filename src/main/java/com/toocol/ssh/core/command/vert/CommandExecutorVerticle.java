package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:08
 */
@PreloadDeployment
public class CommandExecutorVerticle extends AbstractVerticle {

    public static final String ADDRESS_EXECUTE = "ssh.command.execute";

    public static final String ADDRESS_CLEAR = "ssh.command.clear";

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_CLEAR, cmdMessage -> {
            executor.executeBlocking(future -> {
                try {
                    new ProcessBuilder("bash", "-c", "clear")
                            .inheritIO()
                            .start()
                            .waitFor();
                    future.complete("cleared");
                } catch (Exception e) {
                    PrintUtil.printErr("execute command failed!!");
                    future.complete("failed");
                }
            }, res -> {
                eventBus.send(TerminalViewVerticle.ADDRESS_SCREEN_HAS_CLEARED, "cleared");
            });
        });

        eventBus.consumer(ADDRESS_EXECUTE, cmdMessage -> {
            executor.executeBlocking(future -> {
                try {
                    String cmd = String.valueOf(cmdMessage.body());
                    new ProcessBuilder("bash", "-c", cmd)
                            .inheritIO()
                            .start()
                            .waitFor();
                    future.complete(cmd);
                } catch (Exception e) {
                    PrintUtil.printErr("execute command failed!!");
                    if (!future.isComplete()) {
                        future.complete("failed");
                    }
                }
            }, res -> eventBus.send(CommandExecutorVerticle.ADDRESS_CLEAR, null));
        });

        PrintUtil.println("success start the command executor verticle.");
    }
}
