package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermioCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/12 1:18
 * @version: 0.0.1
 */
public class TestCmdProcessor extends TermioCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Term.getInstance().printTest();
    }
}
