package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.mosh.MoshAddress.ESTABLISH_MOSH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/27 16:19
 */
public class MoshCmdProcessor extends TermioCommandProcessor {
    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split(" ");

        if (split.length != 2) {
            resultAndMsg.first(false).second("Wrong mosh cmd, the correct is 'mosh index'");
            return;
        }

        String indexStr = split[1];
        if (!StringUtils.isNumeric(indexStr)) {
            resultAndMsg.first(false).second("The index must be numbers.");
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(indexStr);
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

        eventBus.send(ESTABLISH_MOSH_SESSION.address(), idx);
        resultAndMsg.first(true);
    }
}
