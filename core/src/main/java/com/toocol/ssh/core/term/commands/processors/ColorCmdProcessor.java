package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/7/31 23:32
 * @version: 0.0.1
 */
public class ColorCmdProcessor extends TermioCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Term.getInstance().printColorPanel();
    }
}
