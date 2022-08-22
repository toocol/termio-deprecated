package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.Termio;
import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.ansi.AnsiStringBuilder;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 19:24
 */
public class FlushCmdProcessor extends TermCommandProcessor {
    @Override
    public void process(@NotNull EventBus eventBus, @NotNull String cmd, Tuple2<Boolean, String> resultAndMsg) {
        Printer.clear();
        Term term = Term.instance;
        term.printScene(false);
        term.setCursorPosition(Term.leftMargin, Term.executeLine);

        AnsiStringBuilder builder = new AnsiStringBuilder()
                .background(Term.theme.executeBackgroundColor.color)
                .front(Term.theme.executeFrontColor.color)
                .append(Term.prompt)
                .append(" ".repeat(Termio.windowWidth - Term.getPromptLen() - Term.leftMargin));
        Printer.print(builder.toString());
        term.cleanDisplayBuffer();

        resultAndMsg.first(true);
    }
}
