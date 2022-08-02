package com.toocol.termio.core.shell.commands.processors;

import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.shell.commands.ShellCommandProcessor;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * command: lc
 * desc:    list all the connection properties to quick switch
 *
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/7/28 14:32
 */
public class ShellLcCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        boolean changeSession = shell.switchSession();
        isBreak.set(changeSession);
        StatusCache.SWITCH_SESSION = changeSession;
        return new Tuple2<>(null, shell.getSessionId());
    }
}
