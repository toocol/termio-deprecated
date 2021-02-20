package com.toocol.ssh;

import com.toocol.ssh.common.anno.Deployment;
import com.toocol.ssh.common.anno.OnReadyToDeploy;
import com.toocol.ssh.common.utils.AnnotationUtil;
import com.toocol.ssh.common.utils.CastUtil;
import com.toocol.ssh.common.utils.PrintUtil;
import com.toocol.ssh.core.view.vert.TerminalViewVerticle;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
@OnReadyToDeploy(verticleClass = TerminalViewVerticle.class)
public class TerminalSystem {

    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    public static void main(String[] args) {
        /* get the verticle which need deploy in main class by annotation */
        List<Class<?>> annotatedClassList = AnnotationUtil.getClassListByAnnotation("com.toocol.ssh.core", Deployment.class);
        List<Class<? extends AbstractVerticle>> verticleClassList = new ArrayList<>();
        annotatedClassList.forEach(annotatedClass -> {
            if (annotatedClass.getSuperclass().equals(AbstractVerticle.class)) {
                verticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                PrintUtil.printErr("skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        final CountDownLatch initialLatch = new CountDownLatch(verticleClassList.size());

        /* because of we need to connect ssh, and have some operation, so have to set the check time to the max */
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        Vertx vertx = Vertx.vertx(options);

        /* set the final verticle to deploy*/
        WorkerExecutor executor = vertx.createSharedWorkerExecutor("terminal-system-worker");
        executor.executeBlocking(future -> {
            OnReadyToDeploy annotation = TerminalSystem.class.getAnnotation(OnReadyToDeploy.class);
            if (annotation != null) {
                try {
                    boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
                    if (!ret) {
                        throw new RuntimeException();
                    }
                    vertx.deployVerticle(annotation.verticleClass().getName());
                    future.complete();
                } catch (Exception e) {
                    PrintUtil.printErr("SSH TERMINAL START UP FAILED!!");
                    vertx.close();
                    System.exit(-1);
                }
            }
        }, res -> {
        });

        PrintUtil.printTitle();
        PrintUtil.println("TerminalSystem register the vertx service.");

        verticleClassList.forEach(verticleClass ->
                vertx.deployVerticle(verticleClass.getName(), new DeploymentOptions(), result -> initialLatch.countDown()));
    }
}
