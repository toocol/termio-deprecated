package com.toocol.ssh.core.connector.vert;

import com.toocol.ssh.TerminalSystem;
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

    public static final String ADDRESS_CONNECT = "ssh.connector.connect.prepare";

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS_CONNECT, jsonMessage -> {
            JsonObject message = new JsonObject(String.valueOf(jsonMessage));
            String ip = message.getString("ip");
            String user = message.getString("user");
            String password = message.getString("password");
        });

        PrintUtil.println("success start the ssh connect verticle.");
        TerminalSystem.INITIAL_LATCH.countDown();
    }
}
