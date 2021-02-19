package com.toocol.ssh.core.credential.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
public class SshCredentialReaderVerticle extends AbstractVerticle {

    public static final String ADDRESS = "ssh.credential.reader";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS, jsonMessage -> {

        });

        /* read the storaged ssh credential from file system, if success deploy the view verticle */
        DeploymentOptions options = new DeploymentOptions();
        JsonObject config = new JsonObject();
        config.put("hasCredentials", false);
        options.setConfig(config);
        vertx.deployVerticle(TerminalViewVerticle.class.getName());
        PrintUtil.println("success start the ssh credential reader verticle.");
    }
}
