package com.toocol.ssh.core.credential.vert;

import com.toocol.ssh.common.anno.Deployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;

/**
 * write the ssh credential that user stored to the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:26
 */
@Deployment
public class SshCredentialWriterVerticle extends AbstractVerticle {

    public static final String ADDRESS_WRITE = "ssh.credential.writer";

    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the ssh credential writer verticle.");
    }
}
