package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.eventbus.EventBus;

import static com.toocol.ssh.core.ssh.SshVerticleAddress.ESTABLISH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 12:04
 */
public class CreateSessionProcessor extends AbstractCommandProcessor {
    @Override
    public <T> void process(EventBus eventBus, T param) throws Exception {
        eventBus.send(ESTABLISH_SESSION.address(), null);
    }
}
