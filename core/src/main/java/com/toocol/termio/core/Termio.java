package com.toocol.termio.core;

import com.toocol.termio.core.shell.core.ShellCharEventDispatcher;
import com.toocol.termio.core.term.core.TermCharEventDispatcher;
import com.toocol.termio.utilities.ansi.Printer;
import com.toocol.termio.utilities.module.ModuleDeployment;
import com.toocol.termio.utilities.jni.JNILoader;
import com.toocol.termio.utilities.log.Logger;
import com.toocol.termio.utilities.log.LoggerFactory;
import com.toocol.termio.utilities.utils.CastUtil;
import com.toocol.termio.utilities.utils.ClassScanner;
import com.toocol.termio.utilities.utils.MessageBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 10:13
 */
public abstract class Termio {
    public enum RunType {
        CONSOLE,
        DESKTOP
    }

    protected static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;
    protected static final Logger logger = LoggerFactory.getLogger(Termio.class);

    protected static CountDownLatch initialLatch;
    protected static CountDownLatch loadingLatch;
    protected static List<Class<? extends AbstractVerticle>> verticleClassList = new ArrayList<>();

    protected static RunType runType;
    protected static Vertx vertx;
    protected static EventBus eventBus;

    public static RunType runType() {
        return runType;
    }

    public static Vertx vertx() {
        return vertx;
    }

    public static EventBus eventBus() {
        return eventBus;
    }

    static {
        /* Get the verticle which need to deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = new ClassScanner("com.toocol.termio", clazz -> clazz.isAnnotationPresent(ModuleDeployment.class)).scan();
        annotatedClassList.forEach(annotatedClass -> {
            Class<?> superclass = annotatedClass.getSuperclass();
            if (superclass != null && superclass.getSuperclass().equals(AbstractVerticle.class)) {
                verticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                logger.error("Skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        loadingLatch = new CountDownLatch(1);
    }

    protected static void componentInitialise() {
        if (runType.equals(RunType.CONSOLE)) {
            JNILoader.load();
        }
        TermCharEventDispatcher.init();
        ShellCharEventDispatcher.init();
        Printer.setPrinter(System.out);
    }

    protected static Vertx prepareVertxEnvironment(Set<Class<? extends AbstractVerticle>> ignore) {
        int initialLatchSize = ignore == null ? verticleClassList.size() : verticleClassList.size() - ignore.size();
        initialLatch = new CountDownLatch(initialLatchSize);

        /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
        VertxOptions options = new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        final Vertx vertx = Vertx.vertx(options);
        LoggerFactory.init(vertx);

        /* Deploy the verticle */
        if (ignore != null && !ignore.isEmpty()) {
            verticleClassList = new ArrayList<>(verticleClassList
                    .stream()
                    .filter(clazz -> !ignore.contains(clazz))
                    .toList());
        }
        verticleClassList.sort(Comparator.comparingInt(clazz -> -1 * clazz.getAnnotation(ModuleDeployment.class).weight()));
        verticleClassList.forEach(verticleClass -> {
                    ModuleDeployment deploy = verticleClass.getAnnotation(ModuleDeployment.class);
                    DeploymentOptions deploymentOptions = new DeploymentOptions();
                    if (deploy.worker()) {
                        deploymentOptions.setWorker(true).setWorkerPoolSize(deploy.workerPoolSize()).setWorkerPoolName(deploy.workerPoolName());
                    }
                    vertx.deployVerticle(verticleClass.getName(), deploymentOptions, result -> {
                        if (result.succeeded()) {
                            initialLatch.countDown();
                        } else {
                            MessageBox.setExitMessage("Termio start up failed, verticle = " + verticleClass.getSimpleName());
                            Printer.print("Termio start up failed, verticle = " + verticleClass.getSimpleName());
                            vertx.close();
                            System.exit(-1);
                        }
                    });
                }
        );
        return vertx;
    }
}
