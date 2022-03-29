package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import static com.toocol.ssh.core.command.ClearScreenAddress.ADDRESS_CLEAR;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.*;
import static com.toocol.ssh.core.view.TerminalViewAddress.ADDRESS_SCREEN_HAS_CLEARED;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 18:16
 */
@PreloadDeployment
public class ClearScreenVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_CLEAR.address(), cmdMessage -> {
            executor.executeBlocking(future -> {
                try {
                    new ProcessBuilder(BOOT_TYPE, getExtraCmd(), getClearCmd())
                            .inheritIO()
                            .start()
                            .waitFor();
                    future.complete("cleared");
                } catch (Exception e) {
                    PrintUtil.printErr("execute command failed!!");
                    future.complete("failed");
                }
            }, res -> eventBus.send(ADDRESS_SCREEN_HAS_CLEARED.address(), "cleared"));
        });
    }
}
