package com.toocol.ssh.core.shell.commands.processors;

import com.toocol.ssh.common.utils.StrUtil;
import com.toocol.ssh.core.shell.commands.ShellCommandProcessor;
import com.toocol.ssh.core.shell.core.Shell;
import com.toocol.ssh.core.shell.handlers.DfHandler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.ssh.common.utils.FilePathUtil.*;
import static com.toocol.ssh.core.shell.ShellAddress.START_DF_COMMAND;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:34
 * @version: 0.0.1
 */
public class ShellDfCmdProcessor extends ShellCommandProcessor {

    @Override
    public String process(EventBus eventBus, Promise<Long> promise, Shell shell, AtomicBoolean isBreak, String cmd) {
        String[] split = cmd.trim().replaceAll(" {2,}", StrUtil.SPACE).split(StrUtil.SPACE);
        if (split.length != 2) {
            return EMPTY;
        }

        String inputPath = split[1];
        String user = shell.getUser().get();
        String currentPath = shell.getFullPath().get();

        StringBuilder remotePath = new StringBuilder();
        if (inputPath.startsWith(CURRENT_FOLDER_PREFIX)) {
            if (USER_FOLDER.equals(currentPath)) {
                remotePath.append(ROOT_FOLDER_PREFIX).append(user).append(inputPath.substring(1));
            } else {
                remotePath.append(currentPath).append(inputPath.substring(1));
            }
        } else if (inputPath.startsWith(PARENT_FOLDER_PREFIX)) {
            String[] singleCurrentPaths = currentPath.split("/");
            if (singleCurrentPaths.length <= 1) {
                remotePath.append(ROOT_FOLDER_PREFIX).append(inputPath.substring(1));
            } else {
                for (int idx = 0; idx <= singleCurrentPaths.length - 2; idx ++) {
                    if (StringUtils.isEmpty(singleCurrentPaths[idx])) {
                        continue;
                    }
                    remotePath.append("/").append(singleCurrentPaths[idx]);
                }
                remotePath.append(inputPath.substring(2));
            }
        } else {
            remotePath.append(currentPath).append("/").append(inputPath);
        }

        JsonObject request = new JsonObject();
        request.put("sessionId", shell.getSessionId());
        request.put("remotePath", remotePath.toString());
        request.put("type", DfHandler.DF_TYPE_FILE);

        eventBus.send(START_DF_COMMAND.address(), request);

        return null;
    }

}
