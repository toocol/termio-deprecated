package com.toocol.ssh;

import com.toocol.ssh.common.annotation.VerticleDeployment;
import com.toocol.ssh.common.jni.JNILoader;
import com.toocol.ssh.common.utils.CastUtil;
import com.toocol.ssh.common.utils.ClassScanner;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.config.SystemConfig;
import com.toocol.ssh.core.shell.core.CharEventDispatcher;
import com.toocol.ssh.core.term.core.Printer;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.toocol.ssh.core.term.TermAddress.ADDRESS_ACCEPT_COMMAND;


/**
 * @author ZhaoZhe
 * @email joezane.cn@gmail.com
 * @date 2021/2/19 15:00
 */
public class TermioApplication {

    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    public static void main(String[] args) {
        JNILoader.load();
        if (args.length != 1) {
            Printer.printErr("Wrong boot type.");
            System.exit(-1);
        }
        SystemConfig.BOOT_TYPE = args[0];

        CharEventDispatcher.init();

        CountDownLatch loadingLatch = new CountDownLatch(1);
        Printer.printLoading(loadingLatch);

        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {
        });

        /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
        VertxOptions options = new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        final Vertx vertx = Vertx.vertx(options);

        /* Add shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Printer.println("Termio: shutdown");
                SessionCache.getInstance().stopAll();
                vertx.close();
            } catch (Exception e) {
                Printer.println("Failed to execute shutdown hook.");
            }
        }));

        /* Get the verticle which need to deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = new ClassScanner("com.toocol.ssh.core", clazz -> clazz.isAnnotationPresent(VerticleDeployment.class)).scan();
        List<Class<? extends AbstractVerticle>> preloadVerticleClassList = new ArrayList<>();
        annotatedClassList.forEach(annotatedClass -> {
            if (annotatedClass.getSuperclass().equals(AbstractVerticle.class)) {
                preloadVerticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                Printer.printErr("Skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        final CountDownLatch initialLatch = new CountDownLatch(preloadVerticleClassList.size());

        /* Deploy the verticle */
        preloadVerticleClassList.sort(Comparator.comparingInt(clazz -> -1 * clazz.getAnnotation(VerticleDeployment.class).weight()));
        preloadVerticleClassList.forEach(verticleClass -> {
                    VerticleDeployment deploy = verticleClass.getAnnotation(VerticleDeployment.class);
                    DeploymentOptions deploymentOptions = new DeploymentOptions();
                    if (deploy.worker()) {
                        deploymentOptions.setWorker(true).setWorkerPoolSize(deploy.workerPoolSize()).setWorkerPoolName(deploy.workerPoolName());
                    }
                    vertx.deployVerticle(verticleClass.getName(), deploymentOptions, result -> {
                        if (result.succeeded()) {
                            initialLatch.countDown();
                        } else {
                            Printer.printErr("Terminal start up failed, verticle = " + verticleClass.getSimpleName());
                            vertx.close();
                            System.exit(-1);
                        }
                    });
                }
        );

        try {
            boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
            if (!ret) {
                throw new RuntimeException("Waiting timeout.");
            }
            while (true) {
                if (StatusCache.LOADING_ACCOMPLISH) {
                    loadingLatch.await();
                    vertx.eventBus().send(ADDRESS_ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.FIRST_IN);
                    System.gc();
                    break;
                }
            }
        } catch (Exception e) {
            vertx.close();
            Printer.printErr("Termio start up error, failed to accept command.");
            System.exit(-1);
        }
    }
}
