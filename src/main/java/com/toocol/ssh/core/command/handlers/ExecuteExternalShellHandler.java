package com.toocol.ssh.core.command.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_CLEAR;
import static com.toocol.ssh.core.command.CommandVerticleAddress.ADDRESS_EXECUTE_SHELL;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.BOOT_TYPE;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.getExtraCmd;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:06
 */
public class ExecuteExternalShellHandler extends AbstractCommandHandler {

    public ExecuteExternalShellHandler(Vertx vertx, WorkerExecutor executor) {
        super(vertx, executor);
    }

    @Override
    public IAddress address() {
        return ADDRESS_EXECUTE_SHELL;
    }

    @Override
    public <T> void handle(Message<T> message) {
        executor.executeBlocking(future -> {
            try {
                eventBus.send(ADDRESS_CLEAR.address(), null);

                String cmd = String.valueOf(message.body());
                Process process = new ProcessBuilder(BOOT_TYPE, getExtraCmd(), cmd)
                        .inheritIO()
                        .start();
                process.waitFor();
                process.destroy();
                future.complete(cmd);
            } catch (Exception e) {
                PrintUtil.printErr("execute command failed!!");
                if (!future.isComplete()) {
                    future.complete("failed");
                }
            }
        }, false, res -> {
        });
    }
}
