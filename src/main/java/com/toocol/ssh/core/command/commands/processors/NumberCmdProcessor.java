package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.core.command.commands.OutsideCommandProcessor;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.cache.CredentialCache;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.ESTABLISH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 17:34
 */
public class NumberCmdProcessor extends OutsideCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        if (!StringUtils.isNumeric(cmd)) {
            resultAndMsg.first(false).second("There can't be any spaces or other character between numbers.");
            return;
        }
        int idx = Integer.parseInt(cmd);
        if (idx <= 0) {
            resultAndMsg.first(false).second("The input number must > 0.");
            return;
        }
        if (idx > CredentialCache.credentialsSize()) {
            resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + CredentialCache.credentialsSize() + ".");
            return;
        }

        eventBus.send(ESTABLISH_SESSION.address(), idx);
        resultAndMsg.first(true);
    }
}
