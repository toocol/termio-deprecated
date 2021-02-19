package com.toocol.ssh;

import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.command.vert.CommandAcceptorVerticle;
import com.toocol.ssh.core.command.vert.CommandExecutorVerticle;
import com.toocol.ssh.core.connector.vert.SshConnectorVerticle;
import com.toocol.ssh.core.credential.vert.SshCredentialReaderVerticle;
import com.toocol.ssh.core.credential.vert.SshCredentialWriterVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.CountDownLatch;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
public class TerminalSystem {

    public static final CountDownLatch INITIAL_LATCH = new CountDownLatch(5);

    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    public static void main(String[] args) {
        PrintUtil.printTitle();
        PrintUtil.println("TerminalSystem register the vertx service.");

        /* because of we need to connect ssh, and have some operation, so have to set the check time to the max */
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(SshConnectorVerticle.class.getName());
        vertx.deployVerticle(CommandAcceptorVerticle.class.getName());
        vertx.deployVerticle(CommandExecutorVerticle.class.getName());
        vertx.deployVerticle(SshCredentialWriterVerticle.class.getName());
        vertx.deployVerticle(SshCredentialReaderVerticle.class.getName());
    }
}
