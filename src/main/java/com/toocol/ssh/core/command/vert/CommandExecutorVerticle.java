package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.enums.InsideCommand;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

/**
 * execute the inside(shell) and outside(user)'s command;
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 18:08
 */
@PreloadDeployment
public class CommandExecutorVerticle extends AbstractVerticle {

    public static final String ADDRESS_EXECUTE_SHELL = "ssh.command.execute.shell";

    public static final String ADDRESS_EXECUTE_OUTSIDE = "ssh.command.execute.outside";

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_EXECUTE_SHELL, cmdMessage -> {
            executor.executeBlocking(future -> {
                try {
                    eventBus.send(ClearScreenVerticle.ADDRESS_CLEAR, null);

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
            }, false, res -> {
            });
        });

        eventBus.consumer(ADDRESS_EXECUTE_OUTSIDE, cmdMessage -> {
            executor.executeBlocking(future -> {
                String cmd = String.valueOf(cmdMessage.body());
                if ("show".equals(cmd)) {
                    eventBus.send(ADDRESS_EXECUTE_SHELL, InsideCommand.NEW_WINDOW_OPENSSH.command);
                }
            }, res -> {
            });
        });

        PrintUtil.println("success start the command executor verticle.");
    }
}
