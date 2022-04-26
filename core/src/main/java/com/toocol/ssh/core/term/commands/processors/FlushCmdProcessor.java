package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.AnisStringBuilder;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class FlushCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Printer.printScene(false);
        Term.getInstance().setCursorPosition(4, Term.executeLine);

        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.executeBackgroundColor)
                .front(Term.theme.executeFrontColor)
                .append(Term.PROMPT)
                .append(" ".repeat(Term.getInstance().getWidth() - Term.getPromptLen() - 4));
        Printer.print(builder.toString());
        Term.getInstance().printBackground();

        resultAndMsg.first(true);
    }
}
