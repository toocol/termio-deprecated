package com.toocol.ssh.core.connector.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:15
 */
public class SshConnectorVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the ssh connect verticle.");
    }
}
