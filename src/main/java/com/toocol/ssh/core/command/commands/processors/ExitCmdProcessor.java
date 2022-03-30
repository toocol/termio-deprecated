package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:53
 */
public class ExitCmdProcessor extends AbstractCommandProcessor {
    @Override
    public <T> void process(T param) {
        System.exit(-1);
    }
}
