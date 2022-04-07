package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.command.commands.OutsideCommandProcessor;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.cache.CredentialCache;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.credentials.CredentialVerticleAddress.DELETE_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 18:55
 */
public class DeleteCmdProcessor extends OutsideCommandProcessor {

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split("--");
        if (split.length != 2) {
            resultAndMsg.first(false).second("Wrong 'delete' command, the correct pattern is 'delete --index'.");
            return;
        }
        String indexStr = split[1];
        if (!StringUtils.isNumeric(indexStr)) {
            resultAndMsg.first(false).second("The index must be number.");
            return;
        }
        int index = Integer.parseInt(indexStr);
        if (CredentialCache.credentialsSize() < index) {
            resultAndMsg.first(false).second("The index correspond credential didn't exist.");
            return;
        }

        eventBus.send(DELETE_CREDENTIAL.address(), index, res -> {
            Printer.clear();
            Printer.printScene();
            Printer.printCursorLine();
        });

        resultAndMsg.first(true);
    }

}
