package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermioCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.utilities.anis.AnisStringBuilder;
import com.toocol.termio.utilities.anis.Printer;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class FlushCmdProcessor extends TermioCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Term.getInstance().printScene(false);
        Term.getInstance().setCursorPosition(Term.LEFT_MARGIN, Term.executeLine);

        AnisStringBuilder builder = new AnisStringBuilder()
                .background(Term.theme.executeBackgroundColor)
                .front(Term.theme.executeFrontColor)
                .append(Term.PROMPT)
                .append(" ".repeat(Term.getInstance().getWidth() - Term.getPromptLen() - Term.LEFT_MARGIN));
        Printer.print(builder.toString());
        TermPrinter.DISPLAY_BUFF = StrUtil.EMPTY;

        resultAndMsg.first(true);
    }
}
