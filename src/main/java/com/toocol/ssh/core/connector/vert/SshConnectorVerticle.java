package com.toocol.ssh.core.connector.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 * this is the verticle that execute command line to create ssh connection.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:15
 */
public class SshConnectorVerticle extends AbstractVerticle {

    public static final String ADDRESS = "ssh.connector.connect";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS, jsonMessage -> {
            JsonObject message = new JsonObject(String.valueOf(jsonMessage));
            System.out.println("hello world");
        });
        PrintUtil.println("success start the ssh connect verticle.");
    }
}
