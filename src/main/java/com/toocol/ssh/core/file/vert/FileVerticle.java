package com.toocol.ssh.core.file.vert;

import cn.hutool.json.JSONObject;
import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.annotation.RegisterHandler;
import com.toocol.ssh.common.handler.IHandlerAssembler;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.file.handlers.ReadFileHandler;
import com.toocol.ssh.core.file.handlers.WriteFileHandler;
import com.toocol.ssh.core.credentials.vo.SshCredential;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonArray;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.toocol.ssh.core.file.FileVerticleAddress.ADDRESS_READ_FILE;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@PreloadDeployment(weight = 10)
@RegisterHandler(handlers = {
        ReadFileHandler.class,
        WriteFileHandler.class
})
public class FileVerticle extends AbstractVerticle implements IHandlerAssembler {

    @Override
    public void start() throws Exception {
        final WorkerExecutor executor = vertx.createSharedWorkerExecutor("file-worker");
        assemble(vertx, executor);

        PrintUtil.println("Success start the ssh credential reader verticle.");
    }

}
