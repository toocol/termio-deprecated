package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.OutsideCommandProcessor;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/31 16:21
 */
public class HelpCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.printHelp();

        resultAndMsg.first(true);
    }
}
