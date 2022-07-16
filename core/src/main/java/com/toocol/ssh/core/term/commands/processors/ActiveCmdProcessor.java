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
        List<Integer> idxs = new ArrayList<>();
        String[] split = cmd.trim().replaceAll(" {2,}", " ").split(" ");
        try {
            if (split.length <= 1) {
                resultAndMsg.first(false).second("No connection properties selected");
                return;
            } else if (split.length == 2) {
                if (split[1].contains("-")) {
                    String[] nums = split[1].trim().split("-");
                    if (nums.length > 2) {
                        resultAndMsg.first(false).second("the input format must be num-num");
                        return;
                    } else {
                        for (String num : nums) {
                            if (!StringUtils.isNumeric(num)) {
                                resultAndMsg.first(false).second("The input parameters must be numeric.");
                                return;
                            }
                        }
                        int start = Integer.parseInt(nums[0]);
                        int end = Integer.parseInt(nums[1]);
                        if (start > end) {
                            resultAndMsg.first(false).second("The input parameters must be from small to large");
                            return;
                        } else {
                            for (int i = start; i <= end; i++) {
                                idxs.add(i);
                            }
                        }
                    }
                } else {
                    if (!StringUtils.isNumeric(split[1])) {
                        resultAndMsg.first(false).second("The input parameters must be numeric.");
                        return;
                    } else {
                        int idx = Integer.parseInt(split[1]);
                        if (idx <= 0) {
                            resultAndMsg.first(false).second("The input number must > 0.");
                            return;
                        }
                        if (idx > credentialCache.credentialsSize()) {
                            resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".");
                            return;
                        } else {
                            idxs.add(idx);
                        }
                    }
                }
            } else {
                List<String> list = Arrays.stream(split).filter(e -> !e.equals("active")).collect(Collectors.toList());
                for (String s : list) {
                    if (!StringUtils.isNumeric(s)) {
                        resultAndMsg.first(false).second("The input parameters must be numeric.");
                        return;
                    }
                    int idx = Integer.parseInt(s);
                    if (idx <= 0) {
                        resultAndMsg.first(false).second("The input number must > 0.");
                        return;
                    }
                    if (idx > credentialCache.credentialsSize()) {
                        resultAndMsg.first(false).second("The input number exceeds stored credentials' size, max number should be " + credentialCache.credentialsSize() + ".");
                        return;
                    } else {
                        idxs.add(idx);
                    }
                }
            }

        } catch (Exception e) {
            resultAndMsg.first(false).second("the input number is too long");
        }
        JsonArray jsonArray = new JsonArray(idxs.stream().distinct().collect(Collectors.toList()));
        eventBus.send(ACTIVE_SSH_SESSION.address(), jsonArray);
    }
}







