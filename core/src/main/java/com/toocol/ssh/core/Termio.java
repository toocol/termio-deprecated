package com.toocol.ssh.core;

import com.toocol.ssh.core.cache.MoshSessionCache;
import com.toocol.ssh.core.cache.SshSessionCache;
import com.toocol.ssh.core.cache.StatusCache;
import com.toocol.ssh.core.shell.core.ShellCharEventDispatcher;
import com.toocol.ssh.core.term.core.TermCharEventDispatcher;
import com.toocol.ssh.core.term.handlers.BlockingAcceptCommandHandler;
import com.toocol.ssh.utilities.anis.Printer;
import com.toocol.ssh.utilities.functional.Ignore;
import com.toocol.ssh.utilities.functional.VerticleDeployment;
import com.toocol.ssh.utilities.jni.JNILoader;
import com.toocol.ssh.utilities.log.FileAppender;
import com.toocol.ssh.utilities.log.Logger;
import com.toocol.ssh.utilities.log.LoggerFactory;
import com.toocol.ssh.utilities.utils.CastUtil;
import com.toocol.ssh.utilities.utils.ClassScanner;
import com.toocol.ssh.utilities.utils.MessageBox;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import sun.misc.Signal;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.toocol.ssh.core.term.TermAddress.ACCEPT_COMMAND;
import static com.toocol.ssh.core.term.TermAddress.MONITOR_TERMINAL;

/**
 * @author ZhaoZhe (joezane.cn@gmail.com)
 * @date 2022/8/1 10:13
 */
public class Termio {
    private static final long BLOCKED_CHECK_INTERVAL = 30 * 24 * 60 * 60 * 1000L;
    private static final Logger logger = LoggerFactory.getLogger(Termio.class);

    private static CountDownLatch initialLatch;
    private static CountDownLatch loadingLatch;
    private static List<Class<? extends AbstractVerticle>> verticleClassList = new ArrayList<>();

    private static Vertx vertx;
    private static EventBus eventBus;

    static {
        /* Get the verticle which need to deploy in main class by annotation */
        Set<Class<?>> annotatedClassList = new ClassScanner("com.toocol.ssh.core", clazz -> clazz.isAnnotationPresent(VerticleDeployment.class)).scan();
        annotatedClassList.forEach(annotatedClass -> {
            if (annotatedClass.getSuperclass().equals(AbstractVerticle.class)) {
                verticleClassList.add(CastUtil.cast(annotatedClass));
            } else {
                Printer.printErr("Skip deploy verticle " + annotatedClass.getName() + ", please extends AbstractVerticle");
            }
        });
        initialLatch = new CountDownLatch(verticleClassList.size());
        loadingLatch = new CountDownLatch(1);
    }

    public static void run() {
        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {
        });

        componentInitialise(System.out);
        Printer.printLoading(loadingLatch);

        vertx = prepareVertxEnvironment(null);
        eventBus = vertx.eventBus();

        LoggerFactory.init(vertx);
        addShutdownHook(vertx);
        waitingStart(vertx);
    }

    public static Vertx run(Class<?> runClass, PrintStream printStream) {
        /* Block the Ctrl+C */
        Signal.handle(new Signal("INT"), signal -> {
        });

        componentInitialise(printStream);
        loadingLatch.countDown();

        Ignore ignore = runClass.getAnnotation(Ignore.class);
        Vertx vertx = prepareVertxEnvironment(Arrays.stream(ignore.ignore()).collect(Collectors.toSet()));
        LoggerFactory.init(vertx);
        addShutdownHook(vertx);
        return vertx;
    }

    public static Vertx vertx() {
        return vertx;
    }

    public static EventBus eventBus() {
        return eventBus;
    }

    private static void componentInitialise(PrintStream printStream) {
        JNILoader.load();
        TermCharEventDispatcher.init();
        ShellCharEventDispatcher.init();
        Printer.setPrinter(printStream);
    }

    private static Vertx prepareVertxEnvironment(Set<Class<? extends AbstractVerticle>> ignore) {
        /* Because this program involves a large number of IO operations, increasing the blocking check time, we don't need it */
        VertxOptions options = new VertxOptions()
                .setBlockedThreadCheckInterval(BLOCKED_CHECK_INTERVAL);
        final Vertx vertx = Vertx.vertx(options);

        /* Deploy the verticle */
        if (ignore != null && !ignore.isEmpty()) {
            verticleClassList = verticleClassList
                    .stream()
                    .filter(clazz -> !ignore.contains(clazz))
                    .toList();
        }
        verticleClassList.sort(Comparator.comparingInt(clazz -> -1 * clazz.getAnnotation(VerticleDeployment.class).weight()));
        verticleClassList.forEach(verticleClass -> {
                    VerticleDeployment deploy = verticleClass.getAnnotation(VerticleDeployment.class);
                    DeploymentOptions deploymentOptions = new DeploymentOptions();
                    if (deploy.worker()) {
                        deploymentOptions.setWorker(true).setWorkerPoolSize(deploy.workerPoolSize()).setWorkerPoolName(deploy.workerPoolName());
                    }
                    vertx.deployVerticle(verticleClass.getName(), deploymentOptions, result -> {
                        if (result.succeeded()) {
                            initialLatch.countDown();
                        } else {
                            MessageBox.setExitMessage("Termio start up failed, verticle = " + verticleClass.getSimpleName());
                            vertx.close();
                            System.exit(-1);
                        }
                    });
                }
        );
        return vertx;
    }

    private static void addShutdownHook(Vertx vertx) {
        /* Add shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Printer.clear();
                StatusCache.STOP_PROGRAM = true;
                if (MessageBox.hasExitMessage()) {
                    Printer.println(MessageBox.exitMessage());
                }
                Printer.println("Termio: shutdown");
                SshSessionCache.getInstance().stopAll();
                MoshSessionCache.getInstance().stopAll();
                FileAppender.close();
                vertx.close();
            } catch (Exception e) {
                Printer.printErr("Failed to execute shutdown hook.");
            }
        }));
    }

    private static void waitingStart(Vertx vertx) {
        try {
            boolean ret = initialLatch.await(30, TimeUnit.SECONDS);
            if (!ret) {
                throw new RuntimeException("Waiting timeout.");
            }
            while (true) {
                if (Printer.LOADING_ACCOMPLISH) {
                    loadingLatch.await();
                    vertx.eventBus().send(MONITOR_TERMINAL.address(), null);
                    vertx.eventBus().send(ACCEPT_COMMAND.address(), BlockingAcceptCommandHandler.FIRST_IN);

                    loadingLatch = null;
                    initialLatch = null;
                    verticleClassList = null;
                    System.gc();
                    logger.info("Start termio success.");
                    break;
                }
            }
        } catch (Exception e) {
            vertx.close();
            MessageBox.setExitMessage("Termio start up error.");
            System.exit(-1);
        }
    }

}
