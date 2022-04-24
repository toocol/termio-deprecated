package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.shell.commands.ShellCommand;
import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.term.commands.TermioCommand;
import com.toocol.ssh.common.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 16:21
 */
public class HelpCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        resultAndMsg.first(true).second(TermioCommand.help() + ShellCommand.help());
    }
}
