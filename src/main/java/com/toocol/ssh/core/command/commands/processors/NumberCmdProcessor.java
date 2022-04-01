package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.Tuple;
import com.toocol.ssh.core.cache.Cache;
import com.toocol.ssh.core.command.commands.AbstractCommandProcessor;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.ssh.core.shell.ShellVerticleAddress.ESTABLISH_SESSION;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 17:34
 */
public class NumberCmdProcessor extends AbstractCommandProcessor {
    @SafeVarargs
    @Override
    public final <T> void process(EventBus eventBus, T... param) throws Exception {
        String intStr = param[0].toString().trim();
        Tuple<Boolean, String> resultAndMsg = cast(param[1]);
        if (!StringUtils.isNumeric(intStr)) {
            resultAndMsg.first(false).second("There can't be any spaces or other character between numbers.");
            return;
        }
        int idx = Integer.parseInt(intStr);
        if (idx <= 0) {
            resultAndMsg.first(false).second("The input number must > 0.");
            return;
        }
        if (idx > Cache.credentialsSize()) {
            resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + Cache.credentialsSize() + ".");
            return;
        }

        eventBus.send(ESTABLISH_SESSION.address(), idx);
        resultAndMsg.first(true);
    }
}
