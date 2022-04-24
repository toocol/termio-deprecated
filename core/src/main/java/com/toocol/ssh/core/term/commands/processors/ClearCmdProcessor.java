package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.term.core.HighlightHelper;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.utilities.utils.Tuple2;
import com.toocol.ssh.core.term.core.Term;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class ClearCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Printer.printScene(false);
        Term.getInstance().setCursorPosition(0, Term.executeLine);
        Printer.print(HighlightHelper.assembleColorBackground(Term.PROMPT + " ".repeat(Term.getInstance().getWidth() - Term.PROMPT.length()), Term.theme.executeLineBackgroundColor));

        resultAndMsg.first(true);
    }
}
