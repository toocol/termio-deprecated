package com.toocol.ssh.core.file.vert;

import cn.hutool.json.JSONObject;
import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.CastUtil;
import com.toocol.ssh.common.utils.FileUtils;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.file.vo.SshCredential;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@PreloadDeployment
public class FileReaderVerticle extends AbstractVerticle {

    public static final String ADDRESS_READ_CREDENTIAL = "terminal.file.read.credential";

    private final List<SshCredential> sshCredentials = new ArrayList<>();

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_READ_CREDENTIAL, message -> {
            JsonArray credentialsArray = new JsonArray(sshCredentials);
            message.reply(credentialsArray.toString());
        });

        /* read the stored ssh credential from file system, if success deploy the view verticle */
        Buffer resultBuffer = vertx.fileSystem().readFileBlocking(FileUtils.relativeToFixed("/starter/credentials.json"));
        String credentials = resultBuffer.getString(0, resultBuffer.length());
        if (!StringUtils.isEmpty(credentials)) {
            JsonArray credentialsArray = new JsonArray(credentials);
            credentialsArray.forEach(o -> {
                JSONObject credentialJsonObj = CastUtil.cast(o);
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
        PrintUtil.println("success start the ssh credential reader verticle.");
    }
}
