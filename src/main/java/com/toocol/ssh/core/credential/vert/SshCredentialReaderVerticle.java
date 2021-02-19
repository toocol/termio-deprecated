package com.toocol.ssh.core.credential.vert;

import com.toocol.ssh.TerminalSystem;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.credential.vo.SshCredential;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * read the ssh credential from the local file system.
 *
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 16:27
 */
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
                TerminalSystem.INITIAL_LATCH.await(30, TimeUnit.SECONDS);
                vertx.deployVerticle(TerminalViewVerticle.class.getName());
            } catch (InterruptedException e) {
                System.out.println("SSH TERMINAL START UP FAILED!!");
                vertx.close();
                System.exit(-1);
            }
        });
        PrintUtil.println("success start the ssh credential reader verticle.");
        TerminalSystem.INITIAL_LATCH.countDown();
    }
}
