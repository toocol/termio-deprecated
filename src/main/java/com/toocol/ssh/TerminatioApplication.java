package com.toocol.ssh;

import com.toocol.ssh.common.annotation.FinalDeployment;
import com.toocol.ssh.common.annotation.PreloadDeployment;
import com.toocol.ssh.common.utils.CastUtil;
import com.toocol.ssh.common.utils.ClassScanner;
import com.toocol.ssh.common.utils.Printer;
import com.toocol.ssh.core.cache.SessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.config.SystemConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import javafx.application.Application;
import javafx.stage.Stage;
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
public class TerminatioApplication extends Application {

    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;

    public static void main(String[] args) {
        if (args.length != 1) {
            Printer.printErr("Wrong boot type.");
            System.exit(-1);
        }

        SystemConfig.BOOT_TYPE = args[0];
        Printer.printlnWithLogo("TerminalApplication register the vertx service.");

        /* launch the JavaFX */
        Thread javaFxThread = new Thread(Application::launch);
        javaFxThread.setName("java-fx-app-thread");
        javaFxThread.start();

        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {
        });

        /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        final Vertx vertx = Vertx.vertx(options);

        /* Add shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Printer.println("Terminatio shutdown.");
                SessionCache.getInstance().stopAll();
                vertx.close();
            } catch (Exception e) {
                Printer.println("Failed to execute shutdown hook.");
            }
        }));

        /* Get the preload verticle which need to deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = new ClassScanner("com.toocol.ssh.core", clazz -> clazz.isAnnotationPresent(PreloadDeployment.class)).scan();
        List<Class<? extends AbstractVerticle>> preloadVerticleClassList = new ArrayList<>();
        annotatedClassList.forEach(annotatedClass -> {
            if (annotatedClass.getSuperclass().equals(AbstractVerticle.class)) {
                preloadVerticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                Printer.printErr("Skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        final CountDownLatch initialLatch = new CountDownLatch(preloadVerticleClassList.size());

        /* Deploy the preload verticle */
        preloadVerticleClassList.sort(Comparator.comparingInt(clazz -> -1 * clazz.getAnnotation(PreloadDeployment.class).weight()));
        preloadVerticleClassList.forEach(verticleClass ->
                vertx.deployVerticle(verticleClass.getName(), new DeploymentOptions(), result -> {
                    if (result.succeeded()) {
                        initialLatch.countDown();
                    } else {
                        Printer.printErr("Terminal start up failed, verticle = " + verticleClass.getSimpleName());
                        vertx.close();
                        System.exit(-1);
                    }
                })
        );

        /* Deploy the final verticle */
        vertx.executeBlocking(promise -> {
            Set<Class<?>> finalClassList = new ClassScanner("com.toocol.ssh.core", clazz -> clazz.isAnnotationPresent(FinalDeployment.class)).scan();
            finalClassList.forEach(finalVerticle -> {
                if (!finalVerticle.getSuperclass().equals(AbstractVerticle.class)) {
                    Printer.printErr("Skip deploy verticle " + finalVerticle.getName() + ", please extends AbstractVerticle");
                    return;
                }
                try {
                    boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
                    if (!ret) {
                        throw new RuntimeException();
                    }
                    vertx.deployVerticle(finalVerticle.getName(), complete -> promise.complete());
                } catch (Exception e) {
                    vertx.close();
                    Printer.printErr("Terminatio start up failed.");
                    System.exit(-1);
                }
            });
        }, res -> {
            try {
                while (true) {
                    if (StatusCache.LOADING_ACCOMPLISH) {
                        vertx.eventBus().send(ADDRESS_ACCEPT_COMMAND.address(), 0);
                        System.gc();
                        break;
                    }
                }
            } catch (Exception e) {
                vertx.close();
                Printer.printErr("Terminatio start up error, failed to accept command.");
                System.exit(-1);
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
    }
}
