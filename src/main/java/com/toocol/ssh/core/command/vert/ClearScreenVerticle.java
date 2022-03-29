package com.toocol.ssh.core.command.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;

import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.*;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 18:16
 */
@PreloadDeployment
public class ClearScreenVerticle extends AbstractVerticle {

    public static final String ADDRESS_CLEAR = "ssh.command.clear";

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("command-executor-worker");
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_CLEAR, cmdMessage -> {
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
            }, res -> eventBus.send(TerminalViewVerticle.ADDRESS_SCREEN_HAS_CLEARED, "cleared"));
        });
    }
}
