package com.toocol.ssh.core.file.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.FileUtils;
import com.toocol.ssh.common.utils.PrintUtil;
import io.vertx.core.AbstractVerticle;

/**
 * write the ssh credential that user stored to the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:26
 */
@PreloadDeployment(weight = 1)
public class FileWriterVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        PrintUtil.println("success start the ssh credential writer verticle.");

        boolean success = FileUtils.checkAndCreateFile(FileUtils.relativeToFixed("/starter/credentials.json"));
        if (!success) {
            throw new RuntimeException("Create credential file failed.");
        }

        // TODO: complete the file write logic (write the connection info that user saved)
    }
}
