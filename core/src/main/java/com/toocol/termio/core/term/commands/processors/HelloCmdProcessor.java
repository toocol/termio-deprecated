package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 21:14
 * @version: 0.0.1
 */
public final class HelloCmdProcessor extends TermCommandProcessor {

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Term.instance.printDisplay("Hello you ~");
    }
}
