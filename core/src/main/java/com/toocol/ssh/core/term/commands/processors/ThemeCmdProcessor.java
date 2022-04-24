package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.term.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.core.term.core.TermTheme;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 1:16
 * @version: 0.0.1
 */
public class ThemeCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> tuple) {
        String[] split = cmd.trim().replaceAll(" {2,}"," ").split(" ");
        if (split.length != 2) {
            tuple.first(false).second("Please select the theme [dark/light]");
            return;
        }

        String theme = split[1];
        TermTheme termTheme = TermTheme.nameOf(theme);
        if (termTheme == null) {
            tuple.first(false).second(theme + ": theme not found. support: [dark/light]");
            return;
        }

        Term.theme = termTheme;
        Printer.clear();
        Printer.printScene(false);
        Printer.printTermPrompt();
    }
}
