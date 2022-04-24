package com.toocol.ssh.core.shell.commands;

import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/2 17:42
 */
public abstract class ShellCommandProcessor {

    protected static final String EMPTY = "";

    /**
     * process the shell command
     *
     * @param eventBus event bus
     * @param shell shell
     * @param isBreak break the shell accept cycle
     * @return final cmd should be executed
     */
    public abstract Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd);

}
