package com.toocol.ssh.core.shell.handlers;

import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.Message;

import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.BOOT_TYPE;
import static com.toocol.ssh.core.configuration.vert.ConfigurationVerticle.getExtraCmd;
import static com.toocol.ssh.core.shell.ShellVerticleAddress.ADDRESS_OPEN_SHELL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 20:45
 */
public class OpenShellHandler extends AbstractCommandHandler<Void> {

    public OpenShellHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_OPEN_SHELL;
    }

    @Override
    protected <T> void handleWithin(Future<Void> future, Message<T> message) throws Exception {
        String cmd = cast(message.body());
        Process process = new ProcessBuilder(BOOT_TYPE, getExtraCmd(), cmd)
                .inheritIO()
                .start();
        process.waitFor();
        process.destroy();
    }

    @Override
    protected <T> void resultWithin(AsyncResult<Void> asyncResult, Message<T> message) throws Exception {

    }
}
