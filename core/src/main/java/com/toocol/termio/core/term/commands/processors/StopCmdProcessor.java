package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/5 17:27
 */
public class StopCmdProcessor extends TermCommandProcessor {
    @Override
    public Object process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        return null;
    }
}
