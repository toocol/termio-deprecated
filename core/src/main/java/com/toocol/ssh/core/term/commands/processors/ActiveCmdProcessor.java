package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.ssh.SshAddress.ACTIVE_SSH_SESSION;

/**
 * @author ：JoeZane (joezane.cn@gmail.com)
 * @date: 2022/4/23 20:45
 * @version: 0.0.1
 */
public final class ActiveCmdProcessor extends TermioCommandProcessor {

    private final CredentialCache credentialCache = CredentialCache.getInstance();

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split(" ");
        if (split.length <= 1) {
            resultAndMsg.first(false).second("No connection properties selected.");
            return;
        }
        if (!StringUtils.isNumeric(split[1])) {
            resultAndMsg.first(false).second("There can't be any spaces or other character between numbers.");
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(split[1]);
        } catch (Exception e) {
            resultAndMsg.first(false).second("Number is too long.");
            return;
        }
        if (idx <= 0) {
            resultAndMsg.first(false).second("The input number must > 0.");
            return;
        }
        if (idx > credentialCache.credentialsSize()) {
            resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".");
            return;
        }

        eventBus.send(ACTIVE_SSH_SESSION.address(), idx);
    }
}
