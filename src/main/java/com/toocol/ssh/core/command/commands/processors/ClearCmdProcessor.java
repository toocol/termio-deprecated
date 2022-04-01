package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.Tuple;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class ClearCmdProcessor extends AbstractCommandProcessor {
    @SafeVarargs
    @Override
    public final <T> void process(EventBus eventBus, T... param) throws Exception {
        Printer.clear();
        Printer.printScene();

        Tuple<Boolean, String> resultAndMsg = cast(param[1]);
        resultAndMsg.first(true);
    }
}
