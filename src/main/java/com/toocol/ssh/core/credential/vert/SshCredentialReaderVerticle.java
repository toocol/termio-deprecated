package com.toocol.ssh.core.credential.vert;

import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
public class SshCredentialReaderVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the ssh credential reader verticle.");
    }
}
