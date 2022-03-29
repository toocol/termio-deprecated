package com.toocol.ssh.core.router;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import io.vertx.core.AbstractVerticle;

/**
 * All the message send to this verticle by Vert.x, then it will distribute the message to correspond next Verticle.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/3/19 16:04
 */
@PreloadDeployment
public class MessageRouter extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        //TODO: resolve MessageAddress's @Route annotation
    }
}
