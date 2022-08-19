package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermPrinter;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class FlushCmdProcessor extends TermCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Term.instance.printScene(false);
        Term.instance.setCursorPosition(Term.LEFT_MARGIN, Term.executeLine);

        AnsiStringBuilder builder = new AnsiStringBuilder()
                .background(Term.theme.executeBackgroundColor.color)
                .front(Term.theme.executeFrontColor.color)
                .append(Term.PROMPT)
                .append(" ".repeat(Term.width - Term.getPromptLen() - Term.LEFT_MARGIN));
        Printer.print(builder.toString());
        TermPrinter.displayBuffer = StrUtil.EMPTY;

        resultAndMsg.first(true);
    }
}
