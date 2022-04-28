package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.ssh.SshAddress.ESTABLISH_SSH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 17:34
 */
public class NumberCmdProcessor extends TermioCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        if (!StringUtils.isNumeric(cmd)) {
            resultAndMsg.first(false).second("There can't be any spaces or other character between numbers.");
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(cmd);
        } catch (Exception e) {
            resultAndMsg.first(false).second("Number is too long.");
            return;
        }
        if (idx <= 0) {
            resultAndMsg.first(false).second("The input number must > 0.");
            return;
        }
        if (idx > CredentialCache.credentialsSize()) {
            resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + CredentialCache.credentialsSize() + ".");
            return;
        }

        eventBus.send(ESTABLISH_SSH_SESSION.address(), idx);
        resultAndMsg.first(true);
    }
}