package com.toocol.ssh.core.file.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;

/**
 * write the ssh credential that user stored to the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:26
 */
@PreloadDeployment
public class FileWriterVerticle extends AbstractVerticle {

    public static final String ADDRESS_WRITE = "terminal.file.writer";

    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the ssh credential writer verticle.");
    }
}
