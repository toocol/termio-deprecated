package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        ArrayList<Integer> paramList = new ArrayList<>();
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split(" ");

        if (split.length <= 1) {
            resultAndMsg.first(false).second("No connection properties selected.");
            return;
        }
        if (split.length == 2) {
            if (split[1].contains("-")) {
                String[] strings = split[1].trim().split("-");
                if (strings.length > 2) {
                    resultAndMsg.first(false).second("the input format must be num-num");
                    return;
                }
                for (String string : strings) {
                    if (!StringUtils.isNumeric(string)) {
                        resultAndMsg.first(false).second("The input parameters must be numeric.");
                        return;
                    }
                }
                try {
                    int start = Integer.parseInt(strings[0]);
                    int end = Integer.parseInt(strings[1]);
                    numberCompare(start, end);
                    for (int i = start; i < end + 1; i++) {
                        paramList.add(i);
                    }
                    JsonArray jsonArray = new JsonArray(paramList.stream().distinct().collect(Collectors.toList()));
                    eventBus.send(ACTIVE_SSH_SESSION.address(), jsonArray);
                } catch (Exception e) {
                    resultAndMsg.first(false).second("Number is too long.");
                }
            }
            try {
                if (!StringUtils.isNumeric(split[1])) {
                    resultAndMsg.first(false).second("The input parameters must be numeric.");
                    return;
                }
                int idx = Integer.parseInt(split[1]);
                if (idx <= 0) {
                    resultAndMsg.first(false).second("The input number must > 0.");
                    return;
                }
                if (idx > credentialCache.credentialsSize()) {
                    resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".");
                    return;
                }
                paramList.add(idx);
                JsonArray jsonArray = new JsonArray(paramList);
                eventBus.send(ACTIVE_SSH_SESSION.address(), jsonArray);
            } catch (Exception e) {
                resultAndMsg.first(false).second("Number is too long.");
            }

        }
        List<String> list = Arrays.stream(split).filter(e -> !e.equals("active")).collect(Collectors.toList());
        for (String s : list) {
            if (!StringUtils.isNumeric(s)) {
                resultAndMsg.first(false).second("The input parameters must be numeric.");
                return;
            }
            try {
                int idx = Integer.parseInt(s);
                if (idx <= 0) {
                    resultAndMsg.first(false).second("The input number must > 0.");
                    return;
                }
                if (idx > credentialCache.credentialsSize()) {
                    resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".");
                    return;
                }
                paramList.add(idx);
                JsonArray jsonArray = new JsonArray(paramList);
                eventBus.send(ACTIVE_SSH_SESSION.address(), jsonArray);
            } catch (Exception e) {
                resultAndMsg.first(false).second("Number is too long.");
            }
        }


    }

    public void numberCompare(int numOne, int numTwo) {
        if (numOne > numTwo) {
            int reference = numOne;
            numOne = numTwo;
            numTwo = reference;
        }
    }
}





