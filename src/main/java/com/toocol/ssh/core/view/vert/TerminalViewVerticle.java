package com.toocol.ssh.core.view.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:47
 */
public class TerminalViewVerticle extends AbstractVerticle {

    public static final String ADDRESS = "ssh.terminal.view";

    @Override
    public void start() throws Exception {
        JsonObject config = config();
        Optional<Boolean> hasCredentialsOpt = Optional.ofNullable(config.getBoolean("hasCredentials"));
        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(ADDRESS, showWitch -> {
            PrintUtil.printPromptScene(hasCredentialsOpt.orElse(false));
        });

        PrintUtil.println("success start the ssh terminal view verticle.");
    }
}
