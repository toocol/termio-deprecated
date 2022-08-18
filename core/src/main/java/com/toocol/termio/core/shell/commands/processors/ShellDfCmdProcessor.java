package com.toocol.termio.core.shell.commands.processors;

import com.toocol.termio.core.shell.commands.ShellCommandProcessor;
import com.toocol.termio.core.shell.core.Shell;
import com.toocol.termio.core.shell.handlers.BlockingDfHandler;
import com.toocol.termio.utilities.utils.StrUtil;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.toocol.termio.core.shell.ShellAddress.START_DF_COMMAND;
import static com.toocol.termio.utilities.utils.FilePathUtil.*;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/9 16:34
 * @version: 0.0.1
 */
public class ShellDfCmdProcessor extends ShellCommandProcessor {

    @Override
    public Tuple2<String, Long> process(EventBus eventBus, Shell shell, AtomicBoolean isBreak, String cmd) {
        String[] split = cmd.trim().replaceAll(" {2,}", StrUtil.SPACE).split(StrUtil.SPACE);
        if (split.length < 2) {
            return new Tuple2<>(EMPTY, null);
        }

        StringBuilder remotePath = new StringBuilder();

        for (int pathIndex = 1; pathIndex < split.length; pathIndex++) {
            String inputPath = split[pathIndex];
            String user = shell.user;
            String currentPath = shell.fullPath.get();

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
                    for (int idx = 0; idx <= singleCurrentPaths.length - 2; idx++) {
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

            remotePath.append(",");
        }
        remotePath.deleteCharAt(remotePath.length() - 1);

        JsonObject request = new JsonObject();
        request.put("sessionId", shell.getSessionId());
        request.put("remotePath", remotePath.toString());
        request.put("type", BlockingDfHandler.DF_TYPE_FILE);

        eventBus.send(START_DF_COMMAND.address(), request);

        return new Tuple2<>(null, null);
    }

}
