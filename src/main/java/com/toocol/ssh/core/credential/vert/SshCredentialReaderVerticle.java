package com.toocol.ssh.core.credential.vert;

import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.credential.vo.SshCredential;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import jdk.nashorn.internal.runtime.ECMAException;

import java.util.ArrayList;
import java.util.List;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
@PreloadDeployment
public class SshCredentialReaderVerticle extends AbstractVerticle {

    public static final String ADDRESS = "ssh.credential.reader";

    private final List<SshCredential> sshCredentials = new ArrayList<>();

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer(ADDRESS, jsonMessage -> {

        });

        /* read the stored ssh credential from file system, if success deploy the view verticle */
        vertx.fileSystem().open("F://credentials.json", new OpenOptions(), result -> {
            try {
                AsyncFile asyncFile = result.result();
            } catch (ECMAException e) {
                PrintUtil.printErr("SSH TERMINAL START UP FAILED!!");
                vertx.close();
                System.exit(-1);
            }
        });
        PrintUtil.println("success start the ssh credential reader verticle.");
    }
}
