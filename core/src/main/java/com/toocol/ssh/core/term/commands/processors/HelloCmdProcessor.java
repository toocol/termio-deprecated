package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.utilities.utils.Tuple2;
import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.core.term.core.Term;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 21:14
 * @version: 0.0.1
 */
public final class HelloCmdProcessor extends TermioCommandProcessor {

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> tuple) {
        Term.getInstance().printDisplay("Hello you ~");
    }
}
