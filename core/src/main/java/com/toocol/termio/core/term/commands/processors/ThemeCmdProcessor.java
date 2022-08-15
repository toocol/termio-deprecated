package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.core.term.core.TermTheme;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:16
 * @version: 0.0.1
 */
public class ThemeCmdProcessor extends TermCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split(" ");
        if (split.length != 2) {
            resultAndMsg.first(false).second("Please select the theme, alternative themes:\n\n" + TermTheme.listTheme());
            return;
        }

        String theme = split[1];
        TermTheme termTheme = TermTheme.nameOf(theme);
        if (termTheme == null) {
            resultAndMsg.first(false).second(theme + ": theme not found.  alternative themes:\n\n" + TermTheme.listTheme());
            return;
        }

        Term.theme = termTheme;
        Printer.clear();
        Term.getInstance().printScene(false);
        Term.getInstance().printTermPrompt();
    }
}
