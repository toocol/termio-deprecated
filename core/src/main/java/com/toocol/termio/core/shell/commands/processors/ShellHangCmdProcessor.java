package com.toocol.termio.core.shell.commands.processors;

import com.toocol.termio.core.cache.StatusCache;
import com.toocol.termio.core.shell.commands.ShellCommandProcessor;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 13:52
 */
public class ShellHangCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        shell.cleanUp();
        isBreak.set(true);
        StatusCache.HANGED_QUIT = true;
        return new Tuple2<>(StrUtil.EMPTY, shell.getSessionId());
    }
}
