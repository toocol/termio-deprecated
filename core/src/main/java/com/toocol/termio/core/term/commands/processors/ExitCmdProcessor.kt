package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 14:53
 */
public class ExitCmdProcessor extends TermCommandProcessor {
    @Override
    public Object process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        System.exit(-1);
        return null;
    }
}
