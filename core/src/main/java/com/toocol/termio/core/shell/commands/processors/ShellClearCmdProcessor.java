package com.toocol.termio.core.shell.commands.processors;

import com.toocol.termio.core.shell.commands.ShellCommandProcessor;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:48
 */
public class ShellClearCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        Term.instance.hideCursor();
        Printer.clear();
        shell.print(shell.getPrompt());
        Term.instance.showCursor();
        return new Tuple2<>("clear", null);
    }
}
