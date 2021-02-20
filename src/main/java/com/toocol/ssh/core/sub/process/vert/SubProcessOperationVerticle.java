package com.toocol.ssh.core.sub.process.vert;

import com.toocol.ssh.common.anno.Deployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/20 11:14
 */
@Deployment
public class SubProcessOperationVerticle extends AbstractVerticle {

    public static final String ADDRESS_NEW_GIT_BASH = "sub.process.operation.new.git.bash";

    public static final String ADDRESS_OPENSSH = "sub.process.operation.openssh";

    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the sub process verticle.");
    }
}
