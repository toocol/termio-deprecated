package com.toocol.termio.core.shell.commands.processors;

import com.toocol.termio.core.shell.commands.ShellCommandProcessor;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import com.toocol.termio.core.shell.ShellAddress;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:33
 * @version: 0.0.1
 */
public class ShellUfCmdProcessor extends ShellCommandProcessor {
    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        if (cmd.contains(StrUtil.SPACE)) {
            String ignore = cmd.replaceAll("uf ", "");
            Printer.println("uf: should have no params, ignored '" + ignore.trim() + "'.");
        }
        String remotePath = shell.getFullPath();

        JsonObject request = new JsonObject();
        request.put("sessionId", shell.getSessionId());
        request.put("remotePath", remotePath);

        eventBus.send(ShellAddress.START_UF_COMMAND.address(), request);
        return new Tuple2<>(null, null);
    }
}
