package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 11:48
 */
public class ShellClearCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        Term.getInstance().hideCursor();
        Printer.clear();
        shell.print(shell.getPrompt());
        Term.getInstance().showCursor();
        return new Tuple2<>("clear", null);
    }
}
