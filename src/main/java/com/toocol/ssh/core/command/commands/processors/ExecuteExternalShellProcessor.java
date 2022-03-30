package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.eventbus.EventBus;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.ADDRESS_OPEN_SHELL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:33
 */
public class ExecuteExternalShellProcessor extends AbstractCommandProcessor {

    @Override
    public <T> void process(EventBus eventBus, T param) {
        String cmd = String.valueOf(param);
        eventBus.send(ADDRESS_OPEN_SHELL.address(), cmd);
    }
}
