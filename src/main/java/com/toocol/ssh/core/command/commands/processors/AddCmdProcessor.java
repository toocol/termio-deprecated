package com.toocol.ssh.core.command.commands.processors;

import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.common.utils.RegexUtils;
import com.toocol.ssh.common.utils.Tuple2;
import com.toocol.ssh.core.cache.CredentialCache;
import com.toocol.ssh.core.command.commands.OutsideCommandProcessor;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import static com.toocol.ssh.core.credentials.CredentialVerticleAddress.ADD_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/4/1 16:07
 */
public class AddCmdProcessor extends OutsideCommandProcessor {

    @Override
    public void process(EventBus eventBus, String cmd, Tuple2<Boolean, String> resultAndMsg) {
        String[] split = cmd.replaceAll("\\s*", "").split("--");
        if (split.length != 2) {
            resultAndMsg.first(false).second("Wrong 'add' command, the correct pattern is 'add --host@user@password[@port]'.");
            return;
        }

        String[] params = split[1].split("@");
        if (params.length < 3) {
            resultAndMsg.first(false).second("Wrong 'add' command, the correct pattern is 'add --host@user@password[@port]'.");
            return;
        }

        String host = params[0];
        if (!RegexUtils.matchIp(host)) {
            resultAndMsg.first(false).second("Wrong host format, just supporting Ip address.");
            return;
        }
        String user = params[1];
        String password = params[2];
        int port;
        if (params.length > 3) {
            try {
                port = Integer.parseInt(params[3]);
            } catch (Exception e) {
                resultAndMsg.first(false).second("Port should be numbers.");
                return;
            }
        } else {
            port = 22;
        }

        SshCredential credential = SshCredential.builder().host(host).user(user).password(password).port(port).build();
        if (CredentialCache.containsCredential(credential)) {
            resultAndMsg.first(false).second("Connection property already exist.");
            return;
        }

        eventBus.send(ADD_CREDENTIAL.address(), new JsonObject(credential.toMap()), res -> {
            Printer.clear();
            Printer.printScene();
            Printer.printCursorLine();
        });
        resultAndMsg.first(true);
    }

}
