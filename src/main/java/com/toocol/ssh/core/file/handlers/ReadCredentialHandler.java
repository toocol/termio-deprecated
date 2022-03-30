package com.toocol.ssh.core.file.handlers;

import cn.hutool.json.JSONObject;
import com.toocol.ssh.common.handler.AbstractCommandHandler;
import com.toocol.ssh.common.router.IAddress;
import com.toocol.ssh.common.utils.FileUtils;
import com.toocol.ssh.core.file.vo.SshCredential;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_READ_CREDENTIAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/3/30 11:35
 */
public class ReadCredentialHandler extends AbstractCommandHandler<String> {

    public ReadCredentialHandler(Vertx vertx, WorkerExecutor executor, boolean parallel) {
        super(vertx, executor, parallel);
    }

    @Override
    public IAddress address() {
        return ADDRESS_READ_CREDENTIAL;
    }

    List<SshCredential> sshCredentials = new ArrayList<>();

    @Override
    protected <T> void handleWithin(Future<String> future, Message<T> message) {
        Buffer resultBuffer = vertx.fileSystem().readFileBlocking(FileUtils.relativeToFixed("/starter/credentials.json"));
        String fileData = resultBuffer.getString(0, resultBuffer.length());

        if (!StringUtils.isEmpty(fileData)) {
            JsonArray credentialsArray = new JsonArray(fileData);
            credentialsArray.forEach(o -> {
                JSONObject credentialJsonObj = cast(o);
                SshCredential sshCredential = new SshCredential(
                        credentialJsonObj.getStr("name"),
                        credentialJsonObj.getStr("ip"),
                        credentialJsonObj.getStr("user"),
                        credentialJsonObj.getStr("password"),
                        credentialJsonObj.getStr("note")
                );
                sshCredentials.add(sshCredential);
            });
        }
        future.complete(fileData);
    }

    @Override
    protected <T> void resultWithin(AsyncResult<String> asyncResult, Message<T> message) {
        message.reply(new JsonArray(sshCredentials));
    }
}
