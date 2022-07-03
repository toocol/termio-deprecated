package com.toocol.ssh.core.term.commands.processors;

import com.toocol.ssh.core.auth.core.SshCredential;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.term.commands.TermioCommandProcessor;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.core.Term;
import com.toocol.ssh.utilities.utils.RegexUtils;
import com.toocol.ssh.utilities.utils.Tuple2;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import static com.toocol.ssh.core.auth.AuthAddress.ADD_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:07
 */
public class AddCmdProcessor extends TermioCommandProcessor {

    private final CredentialCache credentialCache = CredentialCache.getInstance();

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.trim().replaceAll(" {2,}"," ").split("--");
        if (split.length != 2) {
            resultAndMsg.first(false).second("Wrong 'add' command, the correct pattern is 'add --host@user -c=password [-p=port]'.");
            return;
        }

        String[] params = split[1].split(" ");
        if (params.length < 2 || params.length > 3) {
            resultAndMsg.first(false).second("Wrong 'add' command, the correct pattern is 'add --host@user -c=password [-p=port]'.");
            return;
        }

        String[] hostUser = params[0].split("@");
        if (hostUser.length != 2) {
            resultAndMsg.first(false).second("Wrong 'add' command, the correct pattern is 'add --host@user -c=password [-p=port]'.");
            return;
        }
        String user = hostUser[0];
        String host = hostUser[1];
        if (!RegexUtils.matchIp(host)) {
            resultAndMsg.first(false).second("Wrong host format, just supporting Ip address.");
            return;
        }

        String[] passwordParam = params[1].split("=");
        if (passwordParam.length != 2) {
            resultAndMsg.first(false).second("Wrong host format, just supporting Ip address.");
            return;
        }
        String password= passwordParam[1];
        int port;
        if (params.length == 3) {
            try {
                String[] portParam = params[2].split("=");
                if (portParam.length != 2) {
                    resultAndMsg.first(false).second("Wrong host format, just supporting Ip address.");
                    return;
                }
                port = Integer.parseInt(portParam[1]);
            } catch (Exception e) {
                resultAndMsg.first(false).second("Port should be numbers.");
                return;
            }
        } else {
            port = 22;
        }

        SshCredential credential = SshCredential.builder().host(host).user(user).password(password).port(port).build();
        if (credentialCache.containsCredential(credential)) {
            resultAndMsg.first(false).second("Connection property already exist.");
            return;
        }

        eventBus.request(ADD_CREDENTIAL.address(), new JsonObject(credential.toMap()), res -> {
            Printer.clear();
            Term.getInstance().printScene(false);
            Term.getInstance().printTermPrompt();
        });
        resultAndMsg.first(true);
    }

}
