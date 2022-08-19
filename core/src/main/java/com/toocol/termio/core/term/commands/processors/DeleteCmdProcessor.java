package com.toocol.termio.core.term.commands.processors;

import com.toocol.termio.core.cache.CredentialCache;
import com.toocol.termio.core.term.commands.TermCommandProcessor;
import com.toocol.termio.core.term.core.Term;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import static com.toocol.termio.core.auth.AuthAddress.DELETE_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 18:55
 */
public class DeleteCmdProcessor extends TermCommandProcessor {

    private final CredentialCache.Instance credentialCache = CredentialCache.Instance;

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split(" ");
        if (split.length != 2) {
            resultAndMsg.first(false).second("Wrong 'delete' command, the correct pattern is 'delete index'.");
            return;
        }
        String indexStr = split[1];
        if (!StringUtils.isNumeric(indexStr)) {
            resultAndMsg.first(false).second("The index must be number.");
            return;
        }
        int index = Integer.parseInt(indexStr);
        if (credentialCache.credentialsSize() < index) {
            resultAndMsg.first(false).second("The index correspond credential didn't exist.");
            return;
        }

        eventBus.request(DELETE_CREDENTIAL.address(), index, res -> {
            Printer.clear();
            Term.instance.printScene(false);
            Term.instance.printTermPrompt();
            Term.instance.setCursorPosition(Term.getPromptLen(), Term.executeLine);
        });

        resultAndMsg.first(true);
    }

}
