package com.toocol.ssh.core.configuration.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 17:46
 */
@PreloadDeployment
public class ConfigurationVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        PrintUtil.println("success start the configuration verticle.");
    }
}
