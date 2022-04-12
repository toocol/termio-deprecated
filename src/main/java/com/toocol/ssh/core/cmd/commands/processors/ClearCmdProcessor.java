package com.toocol.ssh.core.cmd.commands.processors;

import com.toocol.ssh.core.cmd.commands.OutsideCommandProcessor;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class ClearCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Printer.printScene();

        resultAndMsg.first(true);
    }
}
