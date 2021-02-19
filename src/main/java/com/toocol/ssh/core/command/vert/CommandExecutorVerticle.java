package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.TerminalSystem;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:08
 */
public class CommandExecutorVerticle extends AbstractVerticle {

    public static final String ADDRESS_EXECUTE = "ssh.command.execute";

    public static final String ADDRESS_CLEAR = "ssh.command.clear";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_CLEAR, cmdMessage -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
            executor.executeBlocking(future -> {
                try {
                    new ProcessBuilder("bash", "-c", "clear")
                            .inheritIO()
                            .start()
                            .waitFor();
                    future.complete("cleared");
                } catch (Exception e) {
                    PrintUtil.println("execute command failed!!");
                    future.complete("failed");
                }
            }, res -> {
                eventBus.send(TerminalViewVerticle.ADDRESS_SCREEN_HAS_CLEARED, "cleared");
            });
        });
        eventBus.consumer(ADDRESS_EXECUTE, cmdMessage -> {
            WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
            executor.executeBlocking(future -> {
                try {
                    new ProcessBuilder("bash", "-c", String.valueOf(cmdMessage.body()))
                            .inheritIO()
                            .start()
                            .waitFor();
                    future.complete("done");
                    future.complete(cmdMessage.body());
                } catch (Exception e) {
                    PrintUtil.println("execute command failed!!");
                    if (!future.isComplete()) {
                        future.complete("failed");
                    }
                }
            }, res -> {
                eventBus.send(CommandExecutorVerticle.ADDRESS_CLEAR, null);
            });
        });

        PrintUtil.println("success start the command executor verticle.");
        TerminalSystem.INITIAL_LATCH.countDown();
    }

}
