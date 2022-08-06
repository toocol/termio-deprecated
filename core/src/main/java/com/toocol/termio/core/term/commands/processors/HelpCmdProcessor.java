package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.shell.commands.ShellCommand;
import com.toocol.termio.core.term.commands.TermCommand;
import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 16:21
 */
public class HelpCmdProcessor extends TermCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        resultAndMsg.first(true).second(TermCommand.help() + ShellCommand.help());
    }
}
