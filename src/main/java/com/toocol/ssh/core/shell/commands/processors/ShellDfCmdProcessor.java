package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import com.toocol.ssh.core.shell.core.Shell;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.START_DF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:34
 * @version: 0.0.1
 */
public class ShellDfCmdProcessor extends ShellCommandProcessor {
    @Override
    public String process(EventBus eventBus, Promise<Long> promise, long sessionId, AtomicBoolean isBreak, String cmd) {
        String[] split = cmd.trim().replaceAll(" {2,}"," ").split(" ");
        if (split.length != 2) {
            return "";
        }

        String inputPath = split[1];
        Shell shell = SessionCache.getInstance().getShell(sessionId);
        String user = shell.getUser().get();
        String currentPath = shell.getFullPath().get();

        String remotePath = null;
        if (inputPath.startsWith("./")) {
            if ("~".equals(currentPath)) {
                remotePath = "/" + user + inputPath.substring(1);
            } else {
                remotePath = currentPath + inputPath.substring(1);
            }
        } else if (inputPath.startsWith("../")) {
            String[] singlePaths = currentPath.split("/");

        } else if (inputPath.startsWith("/")) {

        } else {

        }

        eventBus.send(START_DF_COMMAND.address(), remotePath);

        return "";
    }
}
